package controller;

import dto.StudentClassDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.ClassService;
import service.ClassService.Access;
import service.StudentService;
import utils.SessionUtil;

public class StudentServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StudentServlet.class.getName());

    private static final String DETAIL_PATH = "/students/detail";
    private static final String LIST_VIEW = "/class/studentlist.jsp";
    private static final String DETAIL_VIEW = "/class/studentdetail.jsp";
    private static final int KEYWORD_MAX = 50;

    private final ClassService classService = new ClassService();
    private final StudentService service = new StudentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User me = SessionUtil.validateSession(request, response);
            if (me == null) {
                response.sendRedirect(request.getContextPath() + "/signin.jsp");
                return;
            }
            String role = classService.getRoleName(me);
            if (ClassService.accessFor(role) != Access.TEACH) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Student List is for teachers.");
                return;
            }
            request.setAttribute("me", me);
            request.setAttribute("meRole", role);
            request.setAttribute("fromServlet", Boolean.TRUE);
            if (DETAIL_PATH.equals(request.getServletPath())) {
                showDetail(request, response, me);
            } else {
                showList(request, response, me);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Students: database error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "We could not reach the database. Please try again in a moment.");
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, User me)
            throws ServletException, IOException, SQLException {
        Integer classId = ClassService.parseIntOrNull(request.getParameter("classId"));
        String keyword = trimToNull(request.getParameter("q"));
        if (keyword != null && keyword.length() > KEYWORD_MAX) {
            keyword = keyword.substring(0, KEYWORD_MAX);
        }
        Integer page = ClassService.parseIntOrNull(request.getParameter("page"));

        request.setAttribute("classes", service.getTeacherClasses(me.getId()));
        request.setAttribute("result", service.getStudents(me.getId(), classId, keyword, page == null ? 1 : page));
        request.setAttribute("fClass", classId == null ? "" : classId.toString());
        request.setAttribute("fKeyword", keyword == null ? "" : keyword);

        List<String> parts = new ArrayList<>();
        if (classId != null) {
            parts.add("classId=" + classId);
        }
        if (keyword != null) {
            parts.add("q=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        }
        request.setAttribute("filterQuery", String.join("&", parts));
        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, User me)
            throws ServletException, IOException, SQLException {
        Integer id = ClassService.parseIntOrNull(request.getParameter("id"));
        User student = id == null ? null : service.findStudent(id);
        List<StudentClassDTO> enrolments = student == null ? List.of() : service.getEnrolments(me.getId(), student.getId());
        if (student == null || enrolments.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "This student is not in any of your classes.");
            return;
        }
        request.setAttribute("student", student);
        request.setAttribute("enrolments", enrolments);
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    private static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    @Override
    public String getServletInfo() {
        return "Class Management: Student List and Student Details";
    }
}
