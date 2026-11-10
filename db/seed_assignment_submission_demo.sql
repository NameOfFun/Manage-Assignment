-- ============================================================================
-- Demo data for testing Assignments + Submissions (optional, local testing only).
-- Run AFTER seed_class_management_demo.sql and seed_student_dashboard_demo.sql, on database "ams".
--
-- Class SE2051-D (teacher.demo) gets 3 groups:
--   Group A: student.demo, student.demo2     (from the student dashboard seed)
--   Group B: student01, student02            (added to the class here)
--   Group C: student03, student04            (added to the class here)
-- and 7 "[Test]" assignments covering every state (open / submitted / graded /
-- overdue / closed / draft / not started). Class SE2051-NET (no groups) gets 1 open
-- assignment to test "student without a group".
--
-- All passwords: 12345678.  Dates are relative to NOW().
-- Running it again does nothing; delete the "[Test]" assignments (and their submissions) to re-create.
-- ============================================================================
DO $$
DECLARE
    c_d        int;
    c_net      int;
    u_teacher  int;
    u_me       int;
    u_friend   int;
    u_s1       int;
    u_s2       int;
    u_s3       int;
    u_s4       int;
    g_a        int;
    g_b        int;
    g_c        int;
    a1 int; a2 int; a3 int; a4 int; a5 int;
BEGIN
    SELECT class_id, teacher_id INTO c_d, u_teacher FROM "Class" WHERE class_code = 'SE2051-D';
    SELECT class_id INTO c_net FROM "Class" WHERE class_code = 'SE2051-NET';
    SELECT user_id INTO u_me FROM "User" WHERE username = 'student.demo';
    SELECT user_id INTO u_friend FROM "User" WHERE username = 'student.demo2';
    SELECT user_id INTO u_s1 FROM "User" WHERE username = 'student01';
    SELECT user_id INTO u_s2 FROM "User" WHERE username = 'student02';
    SELECT user_id INTO u_s3 FROM "User" WHERE username = 'student03';
    SELECT user_id INTO u_s4 FROM "User" WHERE username = 'student04';

    IF c_d IS NULL OR c_net IS NULL OR u_me IS NULL OR u_friend IS NULL OR u_s4 IS NULL THEN
        RAISE EXCEPTION 'Run seed_class_management_demo.sql and seed_student_dashboard_demo.sql first.';
    END IF;
    IF EXISTS (SELECT 1 FROM "ClassAssignment" WHERE class_id = c_d AND title LIKE '[Test]%') THEN
        RAISE NOTICE 'Test assignments already exist - nothing to do.';
        RETURN;
    END IF;

    -- Students of SE2051-D
    INSERT INTO "ClassStudent"(class_id, student_id, enroll_date)
    VALUES (c_d, u_s1, NOW()), (c_d, u_s2, NOW()), (c_d, u_s3, NOW()), (c_d, u_s4, NOW())
    ON CONFLICT DO NOTHING;

    -- Group A already exists (student.demo + student.demo2); create B and C if missing
    SELECT g.group_id INTO g_a FROM "Group" g JOIN "GroupStudent" gs ON gs.group_id = g.group_id
    WHERE g.class_id = c_d AND gs.user_id = u_me ORDER BY g.group_id LIMIT 1;
    IF g_a IS NULL THEN
        INSERT INTO "Group"(class_id) VALUES (c_d) RETURNING group_id INTO g_a;
        INSERT INTO "GroupStudent"(user_id, group_id) VALUES (u_me, g_a), (u_friend, g_a);
    END IF;

    SELECT g.group_id INTO g_b FROM "Group" g JOIN "GroupStudent" gs ON gs.group_id = g.group_id
    WHERE g.class_id = c_d AND gs.user_id = u_s1 ORDER BY g.group_id LIMIT 1;
    IF g_b IS NULL THEN
        INSERT INTO "Group"(class_id) VALUES (c_d) RETURNING group_id INTO g_b;
        INSERT INTO "GroupStudent"(user_id, group_id) VALUES (u_s1, g_b), (u_s2, g_b);
    END IF;

    SELECT g.group_id INTO g_c FROM "Group" g JOIN "GroupStudent" gs ON gs.group_id = g.group_id
    WHERE g.class_id = c_d AND gs.user_id = u_s3 ORDER BY g.group_id LIMIT 1;
    IF g_c IS NULL THEN
        INSERT INTO "Group"(class_id) VALUES (c_d) RETURNING group_id INTO g_c;
        INSERT INTO "GroupStudent"(user_id, group_id) VALUES (u_s3, g_c), (u_s4, g_c);
    END IF;

    -- 1. Open, nobody submitted yet -> first submit (file / link)
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Lab 1 - Database design',
            E'Thiết kế ERD cho hệ thống AMS.\nNộp file PDF hoặc link Google Drive.',
            NOW() - INTERVAL '1 day', NOW() + INTERVAL '7 days', 'Published')
    RETURNING assignment_id INTO a1;

    -- 2. Open, group B submitted (link), A and C not yet
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Lab 2 - Servlet CRUD',
            E'Viết CRUD cho bảng Student bằng Servlet + DAO.\nNộp link GitHub repository.',
            NOW() - INTERVAL '3 days', NOW() + INTERVAL '3 days', 'Published')
    RETURNING assignment_id INTO a2;
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, file_url)
    VALUES (a2, u_s1, g_b, NOW() - INTERVAL '5 hours', 'Submitted', 'https://github.com/example/lab2-group-b');

    -- 3. Open, group A submitted (not graded -> can re-submit), group B graded (cannot re-submit)
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Lab 3 - JSP & JSTL',
            'Hiển thị danh sách Student bằng JSP + JSTL, có phân trang.',
            NOW() - INTERVAL '4 days', NOW() + INTERVAL '2 days', 'Published')
    RETURNING assignment_id INTO a3;
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, file_url)
    VALUES (a3, u_me, g_a, NOW() - INTERVAL '1 day', 'Submitted', 'https://drive.google.com/example-lab3-group-a');
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, teacher_id, score, feedback, evaluated_at, file_url)
    VALUES (a3, u_s2, g_b, NOW() - INTERVAL '2 days', 'Graded', u_teacher, 7.5,
            E'Chạy đúng chức năng.\nThiếu phân trang ở trang cuối.', NOW() - INTERVAL '1 day',
            'https://github.com/example/lab3-group-b');

    -- 4. Deadline passed: group B on time, group C late, group A missing -> teacher grades after deadline
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Lab 4 - Filter & Session',
            'Thêm AuthFilter và quản lý session cho các trang admin.',
            NOW() - INTERVAL '14 days', NOW() - INTERVAL '2 days', 'Published')
    RETURNING assignment_id INTO a4;
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, file_url)
    VALUES (a4, u_s2, g_b, NOW() - INTERVAL '3 days', 'Submitted', 'https://github.com/example/lab4-group-b'),
           (a4, u_s4, g_c, NOW() - INTERVAL '1 day', 'Submitted', 'https://github.com/example/lab4-group-c-late');

    -- 5. Closed before the deadline: group C graded, A and B cannot submit any more
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Quiz review (closed)',
            'Ôn tập trắc nghiệm chương 1-3.',
            NOW() - INTERVAL '5 days', NOW() + INTERVAL '5 days', 'Closed')
    RETURNING assignment_id INTO a5;
    INSERT INTO "Submission"(assignment_id, student_id, group_id, submit_time, status, teacher_id, score, feedback, evaluated_at, file_url)
    VALUES (a5, u_s3, g_c, NOW() - INTERVAL '4 days', 'Graded', u_teacher, 10, 'Xuất sắc.', NOW() - INTERVAL '3 days',
            'https://drive.google.com/example-quiz-group-c');

    -- 6. Draft: students must NOT see it
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Final project (draft)', 'Đề bài đồ án cuối kỳ - đang soạn.',
            NOW() - INTERVAL '1 day', NOW() + INTERVAL '20 days', 'Draft');

    -- 7. Published but starts in 2 days: students must NOT see it yet
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_d, '[Test] Lab 5 - Not started yet', 'Lab 5 sẽ mở sau 2 ngày.',
            NOW() + INTERVAL '2 days', NOW() + INTERVAL '9 days', 'Published');

    -- 8. SE2051-NET has no groups: student01 sees it but cannot submit ("chưa thuộc nhóm nào")
    INSERT INTO "ClassAssignment"(class_id, title, instruction, start_date, due_date, status)
    VALUES (c_net, '[Test] Lab 1 - Class without groups', 'Lớp chưa chia nhóm - student không nộp được.',
            NOW() - INTERVAL '1 day', NOW() + INTERVAL '7 days', 'Published');

    RAISE NOTICE 'Test data created. Teacher: teacher.demo | Group A: student.demo, student.demo2 | Group B: student01, student02 | Group C: student03, student04 (password 12345678)';
END $$;
