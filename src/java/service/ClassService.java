package service;

import dao.ClassDAO;
import dto.ClassDTO;
import dto.ClassResultDTO;
import dto.ClassStudentDTO;
import dto.PageDTO;
import dto.SemesterOptionDTO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import model.Class;
import model.Setting;
import model.User;
import utils.SemesterUtil;

public class ClassService {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_EXPERT = "Expert";
    public static final String ROLE_TEACHER = "Teacher";
    public static final String ROLE_STUDENT = "Student";

    public static final int CLASS_PAGE_SIZE = 10;
    public static final int ROSTER_PAGE_SIZE = 10;

    public static final int CLASS_CODE_MAX = 10;
    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;
    private static final Pattern CLASS_CODE = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    public enum Access {

        MANAGE,
        TEACH,
        NONE
    }

    public static Access accessFor(String roleName) {
        if (ROLE_ADMIN.equalsIgnoreCase(roleName) || ROLE_EXPERT.equalsIgnoreCase(roleName)) {
            return Access.MANAGE;
        }
        if (ROLE_TEACHER.equalsIgnoreCase(roleName)) {
            return Access.TEACH;
        }
        return Access.NONE;
    }

    public String getRoleName(User user) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            return dao.findRoleName(user.getRoleId());
        }
    }

    public PageDTO<ClassDTO> getClasses(String subjectFilter, String semesterFilter, String keyword,
            Integer teacherId, int page) throws SQLException {
        return getClasses(subjectFilter, semesterFilter, keyword, teacherId, null, page);
    }

    public PageDTO<ClassDTO> getClasses(String subjectFilter, String semesterFilter, String keyword,
            Integer teacherId, Integer expertId, int page) throws SQLException {
        ClassDAO.ClassFilter f = new ClassDAO.ClassFilter();
        f.subjectId = parseIntOrNull(subjectFilter);
        SemesterOptionDTO term = parseSemesterOption(semesterFilter);
        if (term != null) {
            f.semesterPrefix = SemesterUtil.prefix(term.getSemester());
            f.year = term.getYear();
        }
        f.keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        f.teacherId = teacherId;
        f.expertId = expertId;

        try (ClassDAO dao = new ClassDAO()) {
            int total = dao.countClasses(f);
            int p = PageDTO.clampPage(page, total, CLASS_PAGE_SIZE);
            List<ClassDTO> rows = dao.getClasses(f, (p - 1) * CLASS_PAGE_SIZE, CLASS_PAGE_SIZE);
            return new PageDTO<>(rows, p, CLASS_PAGE_SIZE, total);
        }
    }
    
    public PageDTO<ClassDTO> getAllClasses(String subjectFilter, String semesterFilter, String keyword,
            Integer teacherId, Integer expertId) throws SQLException {
        ClassDAO.ClassFilter f = new ClassDAO.ClassFilter();
        f.subjectId = parseIntOrNull(subjectFilter);
        SemesterOptionDTO term = parseSemesterOption(semesterFilter);
        if (term != null) {
            f.semesterPrefix = SemesterUtil.prefix(term.getSemester());
            f.year = term.getYear();
        }
        f.keyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        f.teacherId = teacherId;
        f.expertId = expertId;

        try (ClassDAO dao = new ClassDAO()) {
            int total = dao.countClasses(f);
            List<ClassDTO> rows = dao.getClasses(f, 0, total);
            return new PageDTO<>(rows, 0, total, total);
        }
    }

    public List<Setting> getSubjects() throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            return dao.getSubjects();
        }
    }

    public List<Setting> getSubjectsForFilter(Integer teacherId, Integer expertId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            if (teacherId != null) {
                return dao.getSubjectsForTeacher(teacherId);
            }
            if (expertId != null) {
                return dao.getSubjectsForExpert(expertId);
            }
            return dao.getSubjects();
        }
    }

    public List<Setting> getSubjectsForManage(Integer expertId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            if (expertId != null) {
                return dao.getSubjectsForExpert(expertId);
            }
            return dao.getSubjects();
        }
    }

    public boolean canManageClass(ClassDTO c, Integer expertId) throws SQLException {
        if (c == null) {
            return false;
        }
        if (expertId == null) {
            return true;
        }
        try (ClassDAO dao = new ClassDAO()) {
            return dao.isSubjectOfExpert(c.getSubjectId(), expertId);
        }
    }

    public List<User> getTeachers() throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            return dao.getUsersByRoleName(ROLE_TEACHER);
        }
    }

    public List<SemesterOptionDTO> getSemesterOptions(Integer teacherId) throws SQLException {
        return getSemesterOptions(teacherId, null);
    }

    public List<SemesterOptionDTO> getSemesterOptions(Integer teacherId, Integer expertId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            return dao.getSemesterOptions(teacherId, expertId);
        }
    }

    public ClassResultDTO removeClass(int classId) throws SQLException {
        return removeClass(classId, null);
    }

    public ClassResultDTO removeClass(int classId, Integer expertId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            ClassDTO c = dao.findClassById(classId);
            if (c == null) {
                return ClassResultDTO.fail("This class no longer exists.");
            }
            if (expertId != null && !dao.isSubjectOfExpert(c.getSubjectId(), expertId)) {
                return ClassResultDTO.fail("You do not manage the subject of class " + c.getClassCode() + ".");
            }
            if (!c.isRemovable()) {
                return ClassResultDTO.fail("Class " + c.getClassCode() + " cannot be removed because it already has "
                        + describeDependants(c) + ". Remove those first.");
            }
            if (!dao.deleteClass(classId)) {
                return ClassResultDTO
                        .fail("Class " + c.getClassCode() + " could not be removed. Please reload and try again.");
            }
            return ClassResultDTO.ok("Class " + c.getClassCode() + " was removed.", c);
        }
    }

    private static String describeDependants(ClassDTO c) {
        List<String> parts = new ArrayList<>();
        if (c.getGroupCount() > 0) {
            parts.add(c.getGroupCount() + (c.getGroupCount() == 1 ? " group" : " groups"));
        }
        if (c.getAssignmentCount() > 0) {
            parts.add(c.getAssignmentCount() + (c.getAssignmentCount() == 1 ? " assignment" : " assignments"));
        }
        return String.join(" and ", parts);
    }

    public ClassDTO findClass(int classId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            return dao.findClassById(classId);
        }
    }

    public ClassResultDTO createClass(ClassDTO form) throws SQLException {
        return createClass(form, null);
    }

    public ClassResultDTO createClass(ClassDTO form, Integer expertId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            ClassResultDTO result = validate(dao, form, null, expertId);
            if (!result.getErrors().isEmpty()) {
                return result;
            }
            Class created;
            try {
                created = dao.createClass(new Class(0, form.getSubjectId(), form.getTeacherId(),
                        form.getClassCode(), form.getSemester(), form.getYear()));
            } catch (SQLException e) {
                if (isUniqueViolation(e)) {
                    result.addError("classCode", "Class " + form.getClassCode() + " already exists.");
                    return result;
                }
                throw e;
            }
            return ClassResultDTO.ok("Class " + created.getClassCode() + " was created.", convertToClassDTO(created));
        }
    }

    public ClassResultDTO updateClass(ClassDTO form) throws SQLException {
        return updateClass(form, null);
    }

    public ClassResultDTO updateClass(ClassDTO form, Integer expertId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            ClassDTO current = dao.findClassById(form.getId());
            if (current == null) {
                return ClassResultDTO.fail("This class no longer exists.");
            }
            if (expertId != null && !dao.isSubjectOfExpert(current.getSubjectId(), expertId)) {
                return ClassResultDTO.fail("You do not manage the subject of this class.");
            }
            ClassResultDTO result = validate(dao, form, current, expertId);
            if (!result.getErrors().isEmpty()) {
                return result;
            }
            Class saved;
            try {
                saved = dao.updateClass(form.getId(), new Class(form.getId(), form.getSubjectId(),
                        form.getTeacherId(), form.getClassCode(), form.getSemester(), form.getYear()));
            } catch (SQLException e) {
                if (isUniqueViolation(e)) {
                    result.addError("classCode", "Class " + form.getClassCode() + " already exists.");
                    return result;
                }
                throw e;
            }
            if (saved == null) {
                return ClassResultDTO.fail("This class no longer exists.");
            }
            return ClassResultDTO.ok("Class " + saved.getClassCode() + " was updated.", convertToClassDTO(saved));
        }
    }

    private ClassResultDTO validate(ClassDAO dao, ClassDTO form, ClassDTO current, Integer expertId) throws SQLException {
        ClassResultDTO r = ClassResultDTO.fail("Please fix the highlighted fields.");

        String code = form.getClassCode() == null ? "" : form.getClassCode().trim();
        form.setClassCode(code);
        if (code.isEmpty()) {
            r.addError("classCode", "Enter the class name (code).");
        } else if (code.length() > CLASS_CODE_MAX) {
            r.addError("classCode", "The class code can have at most " + CLASS_CODE_MAX + " characters.");
        } else if (!CLASS_CODE.matcher(code).matches()) {
            r.addError("classCode", "Use only letters, digits, '-', '_' or '.', e.g. SE2051-NET.");
        } else {
            Class sameCode = dao.findClassByCode(code);
            if (sameCode != null && (current == null || sameCode.getId() != current.getId())) {
                r.addError("classCode", "Class " + sameCode.getClassCode() + " already exists.");
            }
        }

        if (form.getSubjectId() <= 0) {
            r.addError("subjectId", "Choose a subject.");
        } else if (!dao.isSubject(form.getSubjectId())) {
            r.addError("subjectId", "This subject does not exist.");
        } else if (expertId != null && !dao.isSubjectOfExpert(form.getSubjectId(), expertId)) {
            r.addError("subjectId", "You can only choose a subject that you manage.");
        }

        if (form.getTeacherId() <= 0) {
            r.addError("teacherId", "Choose a teacher.");
        } else {
            boolean unchanged = current != null && current.getTeacherId() == form.getTeacherId();
            User t = dao.findUserById(form.getTeacherId());
            if (t == null) {
                r.addError("teacherId", "This teacher does not exist.");
            } else if (!unchanged && !ROLE_TEACHER.equalsIgnoreCase(dao.findRoleName(t.getRoleId()))) {
                r.addError("teacherId", "This account is not a teacher.");
            }
        }

        String semester = SemesterUtil.normalize(form.getSemester());
        if (semester == null) {
            r.addError("semester", "Choose a semester.");
        } else {
            form.setSemester(semester);
        }

        if (form.getYear() < MIN_YEAR || form.getYear() > MAX_YEAR) {
            r.addError("year", "Choose a year.");
        }
        return r;
    }

    public ClassDTO convertToClassDTO(Class c) {
        return new ClassDTO(c.getId(), c.getSubjectId(), c.getTeacherId(), c.getClassCode(), c.getSemester(),
                c.getYear());
    }

    public List<ClassDTO> convertToClassDTO(List<Class> classList) {
        List<ClassDTO> dtoList = new ArrayList<>();
        for (Class c : classList) {
            dtoList.add(convertToClassDTO(c));
        }
        return dtoList;
    }

    public PageDTO<ClassStudentDTO> getRoster(int classId, int page) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            int total = dao.countStudents(classId);
            int p = PageDTO.clampPage(page, total, ROSTER_PAGE_SIZE);
            return new PageDTO<>(dao.getStudents(classId, (p - 1) * ROSTER_PAGE_SIZE, ROSTER_PAGE_SIZE),
                    p, ROSTER_PAGE_SIZE, total);
        }
    }

    public ClassResultDTO addStudent(int classId, String query) throws SQLException {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) {
            return ClassResultDTO.fail("Enter a student's username, email or full name.");
        }
        try (ClassDAO dao = new ClassDAO()) {
            List<User> found = dao.findStudents(q, ROLE_STUDENT);
            if (found.isEmpty()) {
                return ClassResultDTO.fail("No student matches \"" + q + "\".");
            }
            if (found.size() > 1) {
                return ClassResultDTO.fail(found.size() + " students are named \"" + q
                        + "\". Enter the username or email instead.");
            }
            User s = found.get(0);
            String name = s.getFullName() == null || s.getFullName().isBlank() ? s.getUsername() : s.getFullName();
            if (s.getStatus() != null && !"Active".equalsIgnoreCase(s.getStatus())) {
                return ClassResultDTO.fail(name + "'s account is " + s.getStatus().toLowerCase() + ".");
            }
            if (!dao.addStudent(classId, s.getId())) {
                return ClassResultDTO.fail(name + " is already in this class.");
            }
            return ClassResultDTO.ok(name + " was added to the class.", null);
        }
    }

    public ClassResultDTO removeStudent(int classId, int studentId) throws SQLException {
        try (ClassDAO dao = new ClassDAO()) {
            User s = dao.findUserById(studentId);
            if (s == null || !dao.removeStudent(classId, studentId)) {
                return ClassResultDTO.fail("This student is no longer in the class.");
            }
            String name = s.getFullName() == null || s.getFullName().isBlank() ? s.getUsername() : s.getFullName();
            return ClassResultDTO.ok(name + " was removed from the class.", null);
        }
    }

    public static SemesterOptionDTO parseSemesterOption(String value) {
        if (value == null) {
            return null;
        }
        int dash = value.lastIndexOf('-');
        if (dash <= 0) {
            return null;
        }
        String semester = SemesterUtil.normalize(value.substring(0, dash));
        Integer year = parseIntOrNull(value.substring(dash + 1));
        if (semester == null || year == null) {
            return null;
        }
        return new SemesterOptionDTO(semester, year);
    }

    public static Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isUniqueViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }
}
