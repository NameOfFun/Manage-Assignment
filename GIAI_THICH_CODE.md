# Giải thích code: Teacher Dashboard, Assignment, Submission

Tài liệu tổng hợp các câu hỏi về luồng xử lý, dùng để ôn trước khi trả lời giáo viên.

Mục lục:
1. [Teacher Dashboard: 3 con số trên cùng](#1-teacher-dashboard-3-con-số-trên-cùng)
2. [AssignmentServlet: `id` là gì, `showList` làm gì](#2-assignmentservlet-id-là-gì-showlist-làm-gì)
3. [Create Assignment](#3-create-assignment)
4. [Edit Assignment](#4-edit-assignment)
5. [So sánh Create và Edit](#5-so-sánh-create-và-edit)
6. [`title` có phải là "Assignment name" không](#6-title-có-phải-là-assignment-name-không)
7. [Vì sao vẫn có assignment trùng tên](#7-vì-sao-vẫn-có-assignment-trùng-tên)
8. [Kiểm tra ngày khi Create nằm ở đâu](#8-kiểm-tra-ngày-khi-create-nằm-ở-đâu)
9. [Giải thích đoạn JS kiểm tra ngày ở trang Edit](#9-giải-thích-đoạn-js-kiểm-tra-ngày-ở-trang-edit)
10. [Submission List và Submission Details](#10-submission-list-và-submission-details)

Mọi chức năng đều đi qua 4 tầng: **JSP → Servlet → Service → DAO (SQL)**.

Trong nhiều câu SQL có mẹo `(? = TRUE OR c.teacher_id = ?)`. Tham số đầu là `admin`: nếu là Admin thì vế trái đúng và lấy tất cả. Nếu là Teacher thì chỉ lấy dữ liệu thuộc lớp do giáo viên đó dạy.

---

## 1. Teacher Dashboard: 3 con số trên cùng

### Luồng chung
1. Giáo viên mở `/teacherdashboard`. [AuthFilter.java](src/java/filter/AuthFilter.java) kiểm tra đã đăng nhập và có role Teacher.
2. Servlet [TeacherDashboard.java:38-41](src/java/dashboard/TeacherDashboard.java#L38-L41) gọi 3 hàm của `AssignmentService`:
   ```java
   classes     = service.getCurrentTermClasses(teacher.getId());
   reviews     = service.getAssignmentsToEvaluate(teacher.getId());
   assignments = service.getAssignments(teacher.getId(), false);
   ```
3. Mỗi hàm Service mở một `AssignmentDAO`, gọi hàm DAO tương ứng. Hàm DAO chạy SQL và trả về một `List`.
4. Servlet tính 3 con số từ 3 list đó, gán vào request bằng `setAttribute`, rồi forward sang JSP.
5. [teacherdashboard.jsp:44-54](web/dashboard/teacherdashboard.jsp#L44-L54) in ra `${classCount}`, `${assignmentCount}`, `${pendingReviewCount}`.

### Số 1: "Classes this semester" (`classCount`)
Servlet: `classCount = classes.size()`.

SQL ([AssignmentDAO.java:68-76](src/java/dao/AssignmentDAO.java#L68-L76)):
```sql
WITH current_term AS (
  SELECT year, semester FROM "Class" WHERE teacher_id = ?
  ORDER BY year DESC,
           CASE UPPER(semester) WHEN 'FALL' THEN 3 WHEN 'SUMMER' THEN 2
                                WHEN 'SPRING' THEN 1 ELSE 0 END DESC
  LIMIT 1)
SELECT c.* ... FROM "Class" c
JOIN current_term t ON c.year = t.year AND UPPER(c.semester) = UPPER(t.semester)
WHERE c.teacher_id = ?
```
- Bước 1 (CTE `current_term`): tìm kỳ mới nhất mà giáo viên có lớp. Sắp theo năm giảm dần, cùng năm thì Fall > Summer > Spring, lấy 1 dòng. "Semester hiện tại" **không lấy theo ngày hôm nay**, mà là kỳ mới nhất trong dữ liệu của giáo viên.
- Bước 2: lấy tất cả lớp của giáo viên có cùng `year` và `semester` với kỳ đó.

Ví dụ: giáo viên có lớp Spring 2025, Fall 2025, Fall 2025, Spring 2026. Kỳ được chọn là Spring 2026, nên `classCount = 1`.

List này cũng dùng để hiện tên kỳ: `currentTerm = semester + " " + year` của lớp đầu tiên.

### Số 2: "Assignments created" (`assignmentCount`)
Servlet: `assignmentCount = assignments.size()`.

SQL ([AssignmentDAO.java:150-169](src/java/dao/AssignmentDAO.java#L150-L169)), gọi với `admin = false`, không có bộ lọc:
```sql
SELECT a.* ... FROM "ClassAssignment" a
JOIN "Class" c ON c.class_id = a.class_id
WHERE (false = TRUE OR c.teacher_id = ?)
ORDER BY a.start_date DESC, a.assignment_id DESC
```
- Bảng `ClassAssignment` không có cột người tạo, nên "created" thực chất là **mọi assignment thuộc các lớp của giáo viên**.
- Gồm **mọi kỳ học** và **mọi trạng thái** (Draft, Published, Closed).
- List này cũng dùng cho bảng "Recent assignments": `subList(0, min(5, size))`, tức 5 bài mới nhất.

### Số 3: "Submissions awaiting evaluation" (`pendingReviewCount`)
Servlet ([TeacherDashboard.java:52-53](src/java/dashboard/TeacherDashboard.java#L52-L53)):
```java
reviews.stream().mapToInt(AssignmentDTO::getPendingSubmissionCount).sum()
```

SQL ([AssignmentDAO.java:207-216](src/java/dao/AssignmentDAO.java#L207-L216)):
```sql
SELECT a.assignment_id, ..., COUNT(sub.submission_id) AS pending_submission_count
FROM "ClassAssignment" a
JOIN "Class" c        ON c.class_id = a.class_id
JOIN "Submission" sub ON sub.assignment_id = a.assignment_id
WHERE c.teacher_id = ?
  AND UPPER(COALESCE(a.status,'')) = 'PUBLISHED'
  AND sub.score IS NULL
  AND UPPER(sub.status) = 'SUBMITTED'
GROUP BY a.assignment_id, ...
ORDER BY pending_submission_count DESC, a.due_date ASC
LIMIT 5
```
Một bài nộp được tính là "chờ chấm" khi thỏa cả 4 điều kiện:
1. Thuộc lớp của giáo viên.
2. Assignment ở trạng thái **Published**.
3. Submission có `status = 'SUBMITTED'`.
4. Submission **chưa có điểm** (`score IS NULL`).

`GROUP BY` + `COUNT` cho ra số bài chờ chấm của từng assignment, Servlet cộng lại. Vì dùng `JOIN` (không phải `LEFT JOIN`) với `Submission`, assignment không có bài chờ chấm sẽ không xuất hiện. Nếu tổng > 0, JSP thêm class `is-warn` để tô màu cảnh báo.

**Lưu ý:** câu SQL có `LIMIT 5` vì cùng list này dùng cho bảng top 5 cần chấm. Nên tổng chỉ cộng từ 5 assignment có nhiều bài chờ nhất. Ví dụ có 7 assignment, mỗi cái 2 bài chờ chấm: dashboard hiện **10** thay vì **14**. Cách sửa: thêm một query `COUNT(*)` riêng, cùng điều kiện WHERE nhưng không GROUP BY và không LIMIT.

---

## 2. AssignmentServlet: `id` là gì, `showList` làm gì

`id` là **`assignment_id`**, khóa chính của bảng `ClassAssignment`. Giá trị nằm trên URL và được đọc bằng `request.getParameter("id")` ([AssignmentServlet.java:58](src/java/controller/AssignmentServlet.java#L58)).

Cùng URL `/assignment`, servlet dùng `id` để quyết định mở trang nào:

| URL | `id` | Trang hiển thị |
|---|---|---|
| `/assignment` | không có (null) | Danh sách assignment (`showList`) |
| `/assignment?id=` | rỗng | Danh sách assignment (`showList`) |
| `/assignment?id=5` | `5` | Chi tiết assignment số 5 và các bài nộp |
| `/assignment?action=edit&id=5` | `5` | Form sửa assignment số 5 |
| `/assignment?id=abc` hoặc `id=-1` | không hợp lệ | Lỗi 400 "Invalid assignment ID" |

Hàm `showList` ([AssignmentServlet.java:200-224](src/java/controller/AssignmentServlet.java#L200-L224)) làm 2 việc:
1. **Lấy danh sách assignment** thuộc các lớp giáo viên dạy (Admin thấy tất cả). Có thể lọc theo 4 tham số không bắt buộc: `subjectId`, `classId`, `status`, `keyword` (tìm trong title hoặc instruction). Không truyền gì thì hiện toàn bộ.
2. **Nạp dữ liệu cho các dropdown**: class, subject, template (cho bộ lọc và form tạo mới).

Nếu sửa `id` trên URL thành bài của giáo viên khác: `loadAssignment` ([AssignmentServlet.java:268](src/java/controller/AssignmentServlet.java#L268)) gọi `getAssignmentById(id, userId, admin)` với điều kiện assignment phải thuộc lớp của giáo viên. Không thuộc thì trả 404 "You do not have access to this assignment or it does not exist". Thông báo giống hệt khi `id` không tồn tại, nên người dùng không dò được `id` nào có thật.

**Câu trả lời mẫu:**
> "`id` là `assignment_id`, khóa chính của bảng `ClassAssignment`, truyền qua URL dạng `/assignment?id=5`. Không có `id` hoặc `id` rỗng thì em gọi `showList`, hiển thị danh sách assignment thuộc các lớp của giáo viên, lọc được theo subject, class, status, keyword. Có `id` thì em parse sang số nguyên dương. Sai định dạng thì báo 400. Đúng thì kiểm tra assignment có thuộc lớp của giáo viên không, rồi mở trang chi tiết, hoặc trang edit nếu có `action=edit`."

---

## 3. Create Assignment

Create và Edit cùng gửi `POST /assignment`. Servlet phân biệt bằng tham số ẩn `action`:
```java
boolean update = "update".equals(request.getParameter("action"));   // dòng 155
```

### Bước 1: Mở form
Form tạo mới nằm trên trang danh sách [assignment.jsp:101-166](web/assignment/assignment.jsp#L101-L166). `showList` nạp sẵn `classes`, `subjects`, `templates`.

### Bước 2: Các trường trên form

| Trường | `name` | Ghi chú |
|---|---|---|
| Class | `classId` | Bắt buộc. Option có `data-subject-id`, `data-subject-name` |
| Subject | (không có name) | Chỉ đọc, không gửi lên server |
| Template | `templateId` | Không bắt buộc |
| Assignment name | `title` | Bắt buộc, `maxlength=255` |
| Instruction | `instruction` | Bắt buộc |
| Start date, Due date | `startDate`, `dueDate` | `datetime-local`, bắt buộc |
| Save as | `status` | Chỉ **Draft** hoặc **Published** |

### Bước 3: JavaScript ([assignment.jsp:227-274](web/assignment/assignment.jsp#L227-L274))
- Chọn lớp → `syncClassDetails()`: điền tên môn, **ẩn template không cùng môn**. Nếu template đang chọn bị ẩn thì reset về "No template".
- Chọn template → copy `title` và `instruction` của template vào form.
- `startDate.min` = hôm nay 00:00. `dueDate.min` = max(bây giờ, start date).
- `data-submit-once` + [submit-once.js](web/assets/js/submit-once.js): khóa nút submit sau lần bấm đầu.

JS chỉ giúp người dùng nhập đúng. Người dùng có thể bỏ qua JS, nên **server kiểm tra lại toàn bộ**.

### Bước 4: Servlet ([AssignmentServlet.java:162-188](src/java/controller/AssignmentServlet.java#L162-L188))
```java
assignment.setClassId(Integer.parseInt(request.getParameter("classId")));
assignment.setTemplateId(templateId rỗng ? null : Integer.parseInt(templateId));
assignment.setTitle(...); assignment.setInstruction(...);
assignment.setStartDate(parseDate(...)); assignment.setDueDate(parseDate(...));
assignment.setStatus(...);
service.addAssignment(assignment, user.getId(), isAdmin(role));
```
Thành công thì `redirect /assignment?saved=1`. Đây là mẫu **PRG (Post-Redirect-Get)**: bấm F5 không gửi lại form, không tạo bản ghi trùng.

### Bước 5: Service kiểm tra ([AssignmentService.java:70-99](src/java/service/AssignmentService.java#L70-L99))
1. `classId <= 0` → "Please choose a class."
2. `validateContent()`: title (sau trim) không rỗng, ≤ 255 ký tự; instruction không rỗng; due date phải sau start date.
3. `validateNotPast(assignment, null)`: tạo mới nên luôn kiểm tra. Start date không ở ngày đã qua; due date phải sau bây giờ.
4. `status` chỉ được là `Draft` hoặc `Published`.
5. `templateId` nếu có thì phải > 0.

### Bước 6: Transaction chống trùng tên
```java
dao.beginTransaction();                      // setAutoCommit(false)
dao.lockClass(classId);                      // SELECT pg_advisory_xact_lock(classId)
if (dao.titleExists(classId, title, 0))      // trùng tên trong cùng lớp?
    throw DUPLICATE_TITLE;
int id = dao.createAssignment(...);
dao.commit();                                // lỗi thì rollback()
```
- `titleExists` ([AssignmentDAO.java:326](src/java/dao/AssignmentDAO.java#L326)) so sánh `LOWER(TRIM(title))`: không phân biệt hoa thường, bỏ khoảng trắng hai đầu. Chỉ áp dụng **trong cùng một lớp**.
- `lockClass` dùng advisory lock của PostgreSQL theo `classId`. Nếu 2 request đến cùng lúc (double click, hai tab), request thứ hai phải chờ request thứ nhất commit xong mới được kiểm tra. Khóa tự nhả khi transaction kết thúc.

### Bước 7: Câu INSERT ([AssignmentDAO.java:238-264](src/java/dao/AssignmentDAO.java#L238-L264))
```sql
INSERT INTO "ClassAssignment" (class_id, template_id, title, instruction, start_date, due_date, status)
SELECT c.class_id, t.template_id, ?, ?, ?, ?, ?
FROM "Class" c
LEFT JOIN "AssignmentTemplate" t ON t.template_id = ? AND t.subject_id = c.subject_id
WHERE c.class_id = ?
  AND (? = TRUE OR c.teacher_id = ?)            -- admin, hoặc đúng lớp mình dạy
  AND (? = TRUE OR t.template_id IS NOT NULL)   -- không chọn template, hoặc template hợp lệ
RETURNING assignment_id
```
`INSERT ... SELECT` gộp việc kiểm tra quyền vào chính lệnh INSERT:
- `classId` không phải lớp của giáo viên → SELECT trả 0 dòng → không insert.
- Template phải **cùng môn với lớp**. Không chọn template thì cho qua, lưu `template_id = NULL`. Chọn template sai môn thì `LEFT JOIN` ra NULL → 0 dòng.
- 0 dòng thì trả `0`, servlet báo "The class does not exist or you are not assigned to it."

---

## 4. Edit Assignment

### Bước 1: Mở form sửa (`GET /assignment?action=edit&id=5`)
1. Parse `id`. Sai thì 400.
2. `loadAssignment()` → `findAssignmentById(id, userId, admin)` ([AssignmentDAO.java:266](src/java/dao/AssignmentDAO.java#L266)) với điều kiện `a.assignment_id = ? AND (admin OR c.teacher_id = ?)`. Không có quyền thì 404.
3. Có `action=edit` thì forward sang [editassignment.jsp](web/assignment/editassignment.jsp).

### Bước 2: Form sửa ([editassignment.jsp:80-130](web/assignment/editassignment.jsp#L80-L130))
- 2 input ẩn: `action=update` và `id=${assignment.assignmentId}`.
- **Class và Subject là `readonly`**, không gửi lên server. Không đổi lớp được vì đã có nhóm và bài nộp gắn với lớp.
- **Không có Template**, vì template chỉ dùng khi tạo mới.
- Status có thêm **Closed**.
- Giá trị ban đầu ([dòng 7-11](web/assignment/editassignment.jsp#L7-L11)): `valTitle = formSubmitted ? formTitle : assignment.title`. Lần đầu hiện dữ liệu từ DB; submit bị lỗi thì giữ dữ liệu người dùng vừa nhập.

### Bước 3: Servlet ([AssignmentServlet.java:155-182](src/java/controller/AssignmentServlet.java#L155-L182))
```java
Integer id = parseId(request.getParameter("id"));   // sai → 400
assignment.setAssignmentId(id);
// KHÔNG đọc classId, templateId
if (!service.updateAssignment(...)) → 404 NO_ACCESS
else redirect /assignment?id=5&updated=1           // PRG
```

### Bước 4: Service ([AssignmentService.java:103-136](src/java/service/AssignmentService.java#L103-L136))
1. `validateContent()`: giống Create.
2. Status được là `Draft`, `Published` hoặc `Closed`.
3. Đọc bản ghi hiện tại (`current`) bằng `findAssignmentById`. `null` thì trả `false`.
4. `validateNotPast(assignment, current)`: **chỉ ngày nào bị đổi mới phải ở tương lai**.
   ```java
   startChanged = !sameMinute(new.start, current.start);
   dueChanged   = !sameMinute(new.due,   current.due);
   ```
   Bài đã bắt đầu từ tuần trước vẫn sửa được title. `sameMinute` cắt đến phút vì form chỉ gửi đến phút, DB có thể lưu cả giây.
5. Transaction + `lockClass`, **chỉ kiểm tra trùng tên khi đổi tên**:
   ```java
   boolean renamed = !newTitle.equalsIgnoreCase(current.getTitle().trim());
   if (renamed && dao.titleExists(classId, newTitle, assignmentId)) throw DUPLICATE_TITLE;
   ```
   `excludeAssignmentId = assignmentId` để không so tên với chính bài đang sửa.

### Bước 5: Câu UPDATE ([AssignmentDAO.java:297-312](src/java/dao/AssignmentDAO.java#L297-L312))
```sql
UPDATE "ClassAssignment" a
SET title = ?, instruction = ?, start_date = ?, due_date = ?, status = ?
FROM "Class" c
WHERE c.class_id = a.class_id AND a.assignment_id = ?
  AND (? = TRUE OR c.teacher_id = ?)
```
Không cập nhật `class_id`, `template_id`. Điều kiện quyền kiểm tra lại ngay trong UPDATE: sửa input ẩn `id` thành bài của người khác thì `executeUpdate()` trả 0 → 404.

### Xử lý lỗi (chung cho Create và Edit)
- `IllegalArgumentException` (validate, trùng tên, classId sai) → `showFormError()` ([AssignmentServlet.java:227](src/java/controller/AssignmentServlet.java#L227)): giữ lại dữ liệu đã nhập. Create thì hiện lại trang danh sách kèm lỗi; Edit thì hiện lại trang edit với `formSubmitted = true`.
- `SQLException`: ghi log, báo "Cannot save the assignment...".
- Mọi lỗi trong transaction đều `rollback()` ở Service.

---

## 5. So sánh Create và Edit

| | Create | Edit |
|---|---|---|
| Phân biệt | không có `action` | `action=update` + `id` ẩn |
| Chọn Class | Có (bắt buộc) | Không, chỉ readonly |
| Template | Có (tùy chọn, phải cùng môn) | Không |
| Status | Draft / Published | Draft / Published / Closed |
| Kiểm tra ngày quá khứ | Luôn kiểm tra | Chỉ với ngày bị đổi |
| Kiểm tra trùng tên | Luôn kiểm tra | Chỉ khi đổi tên, bỏ qua chính nó |
| Kiểm tra quyền | `INSERT ... SELECT ... WHERE teacher_id` | `findAssignmentById` + `UPDATE ... WHERE teacher_id` |
| Chống double submit | submit-once.js + advisory lock + transaction | như Create |
| Thành công | redirect `/assignment?saved=1` | redirect `/assignment?id=X&updated=1` |

**"Validate ở JS rồi sao còn validate ở server?"** JS chỉ để người dùng thấy lỗi ngay. Request có thể gửi thẳng bằng Postman hoặc sửa HTML, bỏ qua JS, nên server mới là nơi đảm bảo dữ liệu đúng và đúng quyền.

---

## 6. `title` có phải là "Assignment name" không

Đúng. Cùng một thứ, nhìn ở hai chỗ khác nhau:
- **Giao diện**: nhãn "Assignment name" ([assignment.jsp:137](web/assignment/assignment.jsp#L137), [editassignment.jsp:97](web/assignment/editassignment.jsp#L97)).
- **HTML, Java, DB**: tên là `title`.
  - Input: `<input id="title" name="title" maxlength="255">`
  - Servlet: `request.getParameter("title")`
  - Model/DTO: `getTitle()` / `setTitle()`
  - Cột trong bảng `ClassAssignment`: `title`

Thông báo lỗi trong Service cũng gọi là "Assignment name": *"Assignment name is required and must be at most 255 characters."* ([AssignmentService.java:163](src/java/service/AssignmentService.java#L163)).

> "Trên giao diện em ghi là Assignment name cho dễ hiểu, còn trong code và database là `title`."

---

## 7. Vì sao vẫn có assignment trùng tên

Các bài #13–#16 tên `aa` của lớp SE2041-NET được tạo **trước khi có code kiểm tra trùng**.

- Cả 4 bài có start date **18/10/2026 16:16**. Form không cho chọn start date ở ngày đã qua, nên chúng được tạo vào hoặc trước 18/10.
- `titleExists` được thêm ở commit `86438aa` lúc **18/10/2026 17:23** ("assignment validation and duplicate check"). Trước đó `addAssignment` INSERT thẳng, không kiểm tra tên.
- Các file seed trong [db/](db/) không có tên `'aa'`, nên các bài này được tạo bằng tay qua giao diện.
- 4 id liên tiếp, cùng nội dung: dấu hiệu bấm Save nhiều lần khi chưa có submit-once và advisory lock.

Code kiểm tra chỉ chặn **lúc tạo mới hoặc đổi tên**, không xóa dữ liệu cũ. Edit được viết có chủ ý để bài trùng cũ vẫn sửa được:
```java
// Only a renamed title is checked, so old duplicates can still be edited
```
Vì vậy #16 vẫn chuyển sang Closed được (đổi status, không đổi tên).

**Cách tự kiểm chứng:** Clean and Build, deploy lại, tạo assignment tên `aa` (hoặc ` AA `) cho lớp SE2041-NET. Kết quả đúng là báo lỗi *"This class already has an assignment with the same name..."*. Tạo `aa` cho lớp khác thì vẫn được.

> "Mấy bài trùng này em tạo lúc test, trước khi thêm chức năng kiểm tra trùng tên. Bây giờ tạo mới hoặc đổi tên trùng với bài khác trong cùng lớp thì hệ thống báo lỗi. Dữ liệu cũ em không xóa tự động, và vẫn cho sửa các trường khác nếu không đổi tên."

---

## 8. Kiểm tra ngày khi Create nằm ở đâu

Có 3 chỗ, theo thứ tự chạy:

### 1. Client (JS): [assignment.jsp:261-272](web/assignment/assignment.jsp#L261-L272)
```js
startField.min = today + 'T00:00';                 // start >= hôm nay 00:00
function syncDueMin() {
    dueField.min = startField.value && startField.value > nowValue
                   ? startField.value : nowValue;  // due >= max(start, bây giờ)
}
startField.addEventListener('change', syncDueMin);
```

### 2. Servlet: đọc và parse ngày
**Dòng bắt đầu xử lý ngày**: [AssignmentServlet.java:172-173](src/java/controller/AssignmentServlet.java#L172-L173)
```java
assignment.setStartDate(parseDate(request.getParameter("startDate")));
assignment.setDueDate(parseDate(request.getParameter("dueDate")));
```
Hàm `parseDate` ([AssignmentServlet.java:312-321](src/java/controller/AssignmentServlet.java#L312-L321)) kiểm tra ngày có bị trống không, có đúng định dạng không:
```java
if (value == null || value.isBlank())
    throw new IllegalArgumentException("Please enter the start date and the due date.");
try { return LocalDateTime.parse(value); }
catch (DateTimeParseException e) { throw new IllegalArgumentException("Invalid date/time.", e); }
```

### 3. Service: kiểm tra logic ngày
`addAssignment` gọi ở [AssignmentService.java:74-75](src/java/service/AssignmentService.java#L74-L75):
```java
validateContent(assignment);          // dòng 74
validateNotPast(assignment, null);    // dòng 75
```
a) Due date phải sau start date ([AssignmentService.java:169-172](src/java/service/AssignmentService.java#L169-L172)):
```java
if (start == null || due == null || !due.isAfter(start))
    throw new IllegalArgumentException("Due date must be after the start date.");
```
b) Không ở quá khứ ([AssignmentService.java:143-152](src/java/service/AssignmentService.java#L143-L152)):
```java
boolean startChanged = current == null || ...;   // Create: current = null → luôn true
boolean dueChanged   = current == null || ...;
if (startChanged && start.toLocalDate().isBefore(LocalDate.now()))
    throw new IllegalArgumentException("Start date cannot be in the past.");
if (dueChanged && !due.isAfter(LocalDateTime.now()))
    throw new IllegalArgumentException("Due date must be in the future.");
```
- Start date so theo **ngày**: chọn hôm nay 8h sáng dù bây giờ 3h chiều vẫn hợp lệ.
- Due date so theo **ngày và giờ**: phải sau thời điểm hiện tại.

Mọi lỗi là `IllegalArgumentException`, Servlet bắt ở [AssignmentServlet.java:189-191](src/java/controller/AssignmentServlet.java#L189-L191) và gọi `showFormError`.

---

## 9. Giải thích đoạn JS kiểm tra ngày ở trang Edit

Code ở [editassignment.jsp:135-163](web/assignment/editassignment.jsp#L135-L163). Kiểm tra ngày ngay trên trình duyệt, cùng quy tắc với `validateNotPast` ở server.

### Lưu giá trị ban đầu
```js
const startField = document.getElementById('startDate');
const dueField = document.getElementById('dueDate');
const originalStart = startField.value;
const originalDue = dueField.value;
```
Lưu ngày lúc trang vừa mở (ngày trong DB) để biết sau đó giáo viên có đổi ngày không.

### Tính "hôm nay" và "bây giờ"
```js
const pad = n => String(n).padStart(2, '0');   // 5 → "05"
const now = new Date();
const today = now.getFullYear() + '-' + pad(now.getMonth() + 1) + '-' + pad(now.getDate());
// "2026-10-21"
const nowValue = today + 'T' + pad(now.getHours()) + ':' + pad(now.getMinutes());
// "2026-10-21T09:05"
```
- `getMonth()` đếm từ 0 nên `+ 1`.
- `pad` để mọi phần có độ dài cố định, cùng định dạng `yyyy-MM-ddTHH:mm` với ô `datetime-local`.

**Vì sao so sánh chuỗi được?** Cùng định dạng, cùng độ dài, phần lớn (năm) đứng trước, phần nhỏ (phút) đứng sau. So theo thứ tự chữ cái cho kết quả giống so thời gian: `"2026-10-20T23:59" < "2026-10-21T00:00"`. Thiếu `pad` thì sai, ví dụ `"2026-9-..."` lớn hơn `"2026-10-..."`.

### Hàm `check()`
```js
const startChanged = startField.value !== originalStart;
const dueChanged = dueField.value !== originalDue;
```

Start date:
```js
startField.setCustomValidity(
    startChanged && startField.value && startField.value < today + 'T00:00'
        ? 'Start date cannot be in the past.' : '');
```
Báo lỗi khi thỏa cả 3: đã đổi, không rỗng, nằm trước 00:00 hôm nay.

Due date:
```js
let dueError = '';
if (dueChanged && dueField.value && dueField.value <= nowValue) {
    dueError = 'Due date must be in the future.';
} else if (dueField.value && startField.value && dueField.value <= startField.value) {
    dueError = 'Due date must be after the start date.';
}
dueField.setCustomValidity(dueError);
```
- Lỗi 1: due date đã đổi và không sau bây giờ.
- Lỗi 2: **luôn kiểm tra**: due phải sau start.
- `if / else if` nên mỗi lần chỉ hiện 1 lỗi.

`setCustomValidity`: chuỗi khác rỗng → ô không hợp lệ, trình duyệt **chặn submit** và hiện thông báo. Chuỗi rỗng → xóa lỗi.

### Gắn sự kiện
```js
startField.addEventListener('input', check);
dueField.addEventListener('input', check);
check();
```
- `input` chạy mỗi lần giá trị đổi.
- Đổi start date ảnh hưởng điều kiện "due phải sau start", nên cả 2 ô gọi chung `check()`.
- `check()` cuối chạy một lần khi trang tải xong.

### Vì sao không dùng `min` như form Create?
Assignment đã bắt đầu từ tuần trước có start date ở quá khứ. Nếu đặt `min = hôm nay`, trình duyệt coi ngày cũ không hợp lệ và **chặn lưu**, dù giáo viên chỉ muốn sửa title. Nên code dùng `originalStart`/`originalDue` và chỉ kiểm tra ngày bị đổi, giống `startChanged`/`dueChanged` ở server.

### Ví dụ (hôm nay 21/10/2026 09:05)
Assignment có start 18/10 16:16, due 01/11 16:16:

| Giáo viên làm gì | Kết quả |
|---|---|
| Chỉ sửa title | ✅ Lưu được, ngày 18/10 không bị kiểm tra |
| Đổi start sang 19/10 | ❌ "Start date cannot be in the past." |
| Đổi start sang 21/10 08:00 | ✅ Hôm nay, hợp lệ |
| Đổi due sang 21/10 09:00 | ❌ "Due date must be in the future." |
| Đổi start sang 05/11 (sau due 01/11) | ❌ Ô due báo "Due date must be after the start date." |

> "Đoạn JS lưu ngày ban đầu khi mở form. Mỗi lần người dùng sửa, nó chỉ kiểm tra 'không ở quá khứ' với ngày đã bị đổi, còn 'due phải sau start' thì luôn kiểm tra. Lỗi báo bằng `setCustomValidity` để trình duyệt chặn submit. Server vẫn kiểm tra lại bằng `validateNotPast` vì JS có thể bị bỏ qua."

---

## 10. Submission List và Submission Details

Hai trang dùng chung [SubmissionServlet.java](src/java/controller/SubmissionServlet.java), map trong [web.xml:159-161](web/WEB-INF/web.xml#L159-L161):

| URL | Method | Chức năng |
|---|---|---|
| `/submissions` | GET | **Submission List** |
| `/submissions/detail?id=7` | GET | **Submission Details** |
| `/submissions/detail` | POST | Chấm điểm (score, feedback) |

### 10.0. Bước chung đầu `doGet`/`doPost` ([dòng 46-71](src/java/controller/SubmissionServlet.java#L46-L71))
1. `SessionUtil.validateSession()`: chưa đăng nhập → `signin.jsp`.
2. `managerRoleOrNull()`: chỉ **Teacher** hoặc **Admin**. Role khác → `error.jsp`.
3. Phân nhánh:
   ```java
   if (DETAIL_PATH.equals(request.getServletPath()))   // "/submissions/detail"
       → parse id → showDetail(...)
   else
       → showList(...)
   ```

### 10.1. Submission List (`GET /submissions`)

**Đọc và làm sạch tham số** ([showList, dòng 117-134](src/java/controller/SubmissionServlet.java#L117-L134)):

| Tham số | Xử lý |
|---|---|
| `classId`, `assignmentId` | `parseId` → số nguyên dương, sai thì `null` (bỏ lọc) |
| `status` | Chỉ nhận `pending`, `graded`, `late` (whitelist). Khác thì rỗng |
| `keyword` | `trim()`, cắt tối đa 100 ký tự |
| `page` | `parseId`, mặc định 1 |

Các giá trị lọc được gán lại vào request để form lọc giữ nguyên lựa chọn.

**Nạp dữ liệu** ([dòng 135-140](src/java/controller/SubmissionServlet.java#L135-L140)):
```java
classes     = assignmentService.getClasses(...)       // dropdown Class
assignments = assignmentService.getAssignments(...)   // dropdown Assignment
result      = service.getSubmissionPage(..., page, PAGE_SIZE = 15)
```

**Phân trang trong Service** ([SubmissionService.java:49-58](src/java/service/SubmissionService.java#L49-L58)):
```java
int total   = dao.countSubmissions(...);                    // đếm tổng
int current = PageDTO.clampPage(page, total, pageSize);     // ép page vào [1, tổng số trang]
List items  = dao.findSubmissions(..., (current-1)*pageSize, pageSize);
return new PageDTO<>(items, current, pageSize, total);
```
- `clampPage`: `page=999` hoặc `page=-1` được kéo về trang hợp lệ.
- `offset = (trang - 1) × 15`.
- [PageDTO](src/java/dto/PageDTO.java) tính `totalPages`, `hasPrev`, `hasNext`, `from`/`to`, `pageNumbers` (có dấu "…").

**SQL** ([SubmissionDAO.java:100-195](src/java/dao/SubmissionDAO.java#L100-L195)). Phần SELECT dùng chung (`SUBMISSION_SELECT`):
```sql
FROM "Submission" s
JOIN "ClassAssignment" a ON a.assignment_id = s.assignment_id   -- bài tập
JOIN "Class" c           ON c.class_id = a.class_id             -- lớp (kiểm tra quyền)
LEFT JOIN "Settings" sub ON sub.settingid = c.subject_id        -- tên môn
LEFT JOIN "User" su      ON su.user_id = s.student_id           -- sinh viên nộp
LEFT JOIN "User" gr      ON gr.user_id = s.teacher_id           -- người chấm
+ subquery string_agg(...) AS members                           -- tên thành viên nhóm
```

WHERE ghép động (`listWhere`, [dòng 119-140](src/java/dao/SubmissionDAO.java#L119-L140)):
```sql
WHERE (? = TRUE OR c.teacher_id = ?)            -- luôn có: chỉ lớp mình dạy
  AND c.class_id = ?                            -- nếu lọc lớp
  AND a.assignment_id = ?                       -- nếu lọc bài tập
  AND s.score IS NULL                           -- pending
  AND s.score IS NOT NULL                       -- graded
  AND s.submit_time > a.due_date                -- late
  AND (a.title ILIKE ? OR su.full_name ILIKE ? OR su.username ILIKE ?
       OR EXISTS (thành viên nhóm khớp))        -- nếu có keyword
```
- `bindListWhere` gán tham số đúng thứ tự; keyword gán 5 lần dạng `%keyword%`.
- `countSubmissions` và `findSubmissions` dùng cùng `listWhere`, nên số đếm luôn khớp danh sách.

Sắp xếp:
```sql
ORDER BY (s.score IS NULL) DESC,     -- bài chưa chấm lên đầu
         s.submit_time DESC NULLS LAST,
         s.submission_id DESC
LIMIT ? OFFSET ?
```

**JSP** [submissionlist.jsp](web/submission/submissionlist.jsp):
- Form lọc `method="get"`: bộ lọc nằm trên URL, bookmark được.
- JS ([dòng 169-185](web/submission/submissionlist.jsp#L169-L185)): chọn Class thì dropdown Assignment chỉ hiện bài của lớp đó.
- Mỗi dòng link `submissions/detail?id=${s.submissionId}`.
- Link phân trang dùng `<c:url>` + `<c:param>` để giữ bộ lọc ([dòng 150-154](web/submission/submissionlist.jsp#L150-L154)).
- DB lỗi thì trả list rỗng cùng thông báo, không crash.

### 10.2. Submission Details (`GET /submissions/detail?id=7`)

`id` ở đây là **`submission_id`**, khóa chính bảng `Submission` (bài nộp, không phải assignment).

**Servlet** ([showDetail, dòng 151-167](src/java/controller/SubmissionServlet.java#L151-L167)):
```java
Integer id = parseId(request.getParameter("id"));   // sai → 400
SubmissionDTO submission = service.getSubmissionDetail(id, user.getId(), isAdmin(role));
if (submission == null) → 404 NO_ACCESS
request.setAttribute("submission", submission);
request.setAttribute("canGrade", canGrade(submission, user, role));
```

**SQL** ([findSubmissionDetail, dòng 198-208](src/java/dao/SubmissionDAO.java#L198-L208)):
```sql
SUBMISSION_SELECT
WHERE s.submission_id = ? AND (? = TRUE OR c.teacher_id = ?)
```
Đổi `id` sang bài nộp lớp khác → `null` → 404, thông báo giống khi `id` không tồn tại.

**Dữ liệu hiển thị** ([SubmissionDTO](src/java/dto/SubmissionDTO.java)):
- `isLate()` ([dòng 202](src/java/dto/SubmissionDTO.java#L202)): `submitTime > dueDate`.
- `isGraded()` ([dòng 214](src/java/dto/SubmissionDTO.java#L214)): `score != null`.
- `getScoreText()`: hiện `8` thay vì `8.0`, chưa chấm thì `-`.
- `file_url` có 2 dạng: `upload:a1_g2_123456_baocao.pdf` (file trên server, `getFileName()` chỉ hiện `baocao.pdf`) hoặc link ngoài.

**Mở file nộp** ([SubmissionFileServlet](src/java/controller/SubmissionFileServlet.java), `/submission/file?id=7`):
1. `findAccessibleFileUrl()` kiểm tra quyền lại. Không có quyền → 404.
2. Link ngoài → `redirect`.
3. File upload:
   - `resolveStoredFile()` ghép đường dẫn, `normalize()`, kiểm tra vẫn nằm trong thư mục upload. Chống *path traversal* (ví dụ `../../etc/passwd`).
   - Gửi file với `Content-Disposition: attachment` và `nosniff`.
   - Thư mục upload ở **ngoài web app** (`~/ams-uploads/submissions` hoặc cấu hình trong web.xml), deploy lại không mất file.

### 10.3. Chấm điểm (`POST /submissions/detail`)

**Form** ([submissiondetail.jsp:122-145](web/submission/submissiondetail.jsp#L122-L145)): chỉ hiện khi `canGrade = true`. Có `id` ẩn, `score` (`number`, 0–10, `step=0.01`), `feedback` (`maxlength=2000`), `data-submit-once`.

**Servlet `doPost`** ([dòng 74-115](src/java/controller/SubmissionServlet.java#L74-L115)):
```java
id = parseId(...)                                    // sai → 400
submission = service.getSubmissionDetail(id, ...)    // lấy bài nộp + kiểm tra quyền xem
if (submission == null || !canGrade(...)) → 403
service.grade(id, submission.getAssignmentId(), score, feedback, ...)
redirect /submissions/detail?id=7&graded=1           // PRG
```
`canGrade` ([dòng 186](src/java/controller/SubmissionServlet.java#L186)): Admin, hoặc Teacher và `classTeacherId == user.getId()`.

**Service `grade`** ([SubmissionService.java:67-86](src/java/service/SubmissionService.java#L67-L86)):
1. Parse điểm: `trim()`, đổi `,` thành `.` (`8,5` hợp lệ). Không phải số thì lỗi.
2. Điểm từ 0 đến 10, không `NaN`.
3. Feedback ≤ 2000 ký tự, rỗng thì lưu `NULL`.
4. Làm tròn 2 chữ số: `Math.round(score * 100) / 100.0`.

**SQL** ([gradeSubmission, SubmissionDAO.java:79-96](src/java/dao/SubmissionDAO.java#L79-L96)):
```sql
UPDATE "Submission" s
SET score = ?, feedback = ?, status = 'Graded', teacher_id = ?, evaluated_at = NOW()
FROM "ClassAssignment" a JOIN "Class" c ON c.class_id = a.class_id
WHERE a.assignment_id = s.assignment_id
  AND s.submission_id = ? AND s.assignment_id = ?
  AND (? = TRUE OR c.teacher_id = ?)          -- kiểm tra quyền lần cuối
```
Lưu luôn ai chấm (`teacher_id`) và lúc nào (`evaluated_at`).

**Xử lý lỗi**: `IllegalArgumentException` → gán `gradeError`, `formScore`, `formFeedback`, hiện lại trang giữ dữ liệu đã nhập. `SQLException` → ghi log, báo "Cannot save the score…".

### 10.4. Tóm tắt để trả lời giáo viên
> "Submission List và Details dùng chung `SubmissionServlet`, phân biệt bằng `getServletPath()`. Trang List đọc bộ lọc class, assignment, status, keyword, page và kiểm tra từng giá trị: status phải nằm trong whitelist, keyword tối đa 100 ký tự. Service đếm tổng trước, kéo số trang về khoảng hợp lệ, rồi lấy 15 dòng bằng `LIMIT/OFFSET`. SQL luôn có `c.teacher_id = ?` nên giáo viên chỉ thấy bài nộp lớp mình. Bài chưa chấm xếp lên đầu. Trang Details nhận `id` là `submission_id`, kiểm tra quyền ngay trong SQL. Chỉ giáo viên của lớp hoặc admin thấy form chấm. Điểm 0–10, chấp nhận dấu phẩy. Chấm xong lưu người chấm và thời gian, rồi redirect theo PRG."

Code có **2 chỗ chấm điểm**: Assignment Details (`/assignment`, `action=grade`) và Submission Details (`/submissions/detail`). Cả hai gọi chung `SubmissionService.grade()`, nên quy tắc kiểm tra giống hệt.
