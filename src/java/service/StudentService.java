package service;

import dao.ClassDAO;
import dao.StudentDAO;
import dto.ClassDTO;
import dto.PageDTO;
import dto.StudentClassDTO;
import dto.StudentRowDTO;
import java.sql.SQLException;
import java.util.List;
import model.User;

public class StudentService {

    public static final int STUDENT_PAGE_SIZE = 10;
    private static final int MAX_CLASSES = 200;

    public List<ClassDTO> getTeacherClasses(int teacherId) throws SQLException {
        ClassDAO.ClassFilter f = new ClassDAO.ClassFilter();
        f.teacherId = teacherId;
        try (ClassDAO dao = new ClassDAO()) {
            return dao.getClasses(f, 0, MAX_CLASSES);
        }
    }

    public PageDTO<StudentRowDTO> getStudents(int teacherId, Integer classId, String keyword, int page)
            throws SQLException {
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        try (StudentDAO dao = new StudentDAO()) {
            int total = dao.countStudents(teacherId, classId, kw);
            int p = PageDTO.clampPage(page, total, STUDENT_PAGE_SIZE);
            return new PageDTO<>(dao.getStudents(teacherId, classId, kw, (p - 1) * STUDENT_PAGE_SIZE, STUDENT_PAGE_SIZE),
                    p, STUDENT_PAGE_SIZE, total);
        }
    }

    public User findStudent(int studentId) throws SQLException {
        try (StudentDAO dao = new StudentDAO()) {
            return dao.findUser(studentId);
        }
    }

    public List<StudentClassDTO> getEnrolments(int teacherId, int studentId) throws SQLException {
        try (StudentDAO dao = new StudentDAO()) {
            return dao.getEnrolments(teacherId, studentId);
        }
    }
}
