package controller;

import dto.ClassDTO;
import dto.StudentAssignmentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.StudentAssignmentService;
import service.SubmissionService;
import utils.SessionUtil;

/**
 * /myassignments?classId=3 GET  → the assignments of one of the student's classes
 * /myassignments?id=5      GET  → one assignment with its submit form
 * /myassignments?id=5      POST → submit a file or a link for the student's group
 * /myassignments           GET  → back to the dashboard, where the student chooses a class
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2 MB
        maxFileSize = StudentAssignmentService.MAX_FILE_SIZE,
        maxRequestSize = StudentAssignmentService.MAX_FILE_SIZE + 1024 * 1024
)
public class StudentAssignmentServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StudentAssignmentServlet.class.getName());
    private static final String LIST_VIEW = "/student/myassignments.jsp";
    private static final String DETAIL_VIEW = "/student/submitassignment.jsp";
    private static final int PAGE_SIZE = 10;
    private static final String NOT_FOUND = "This assignment does not exist or is not available to you.";
    private static final String CLASS_NOT_FOUND = "This class does not exist or you are not enrolled in it.";
    private StudentAssignmentService service;

    @Override
    public void init() {
        service = new StudentAssignmentService(SubmissionService.fromContext(getServletContext()));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User student = currentStudent(request, response);
        if (student == null) {
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            String classIdParam = request.getParameter("classId");
            if (classIdParam == null || classIdParam.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/studentdashboard");
                return;
            }
            showClassAssignments(request, response, student, parseId(classIdParam));
            return;
        }
        request.setAttribute("submitted", "1".equals(request.getParameter("submitted")));
        showDetail(request, response, student, parseId(idParam));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User student = currentStudent(request, response);
        if (student == null) {
            return;
        }
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            showDetail(request, response, student, null);
            return;
        }

        Part file;
        try {
            file = request.getPart("file");
        } catch (IllegalStateException e) {
            request.setAttribute("formError", "The file cannot be larger than 20 MB.");
            showDetail(request, response, student, id);
            return;
        }

        String link = request.getParameter("link");
        try {
            if (!service.submit(student.getId(), id, file, link)) {
                showDetail(request, response, student, null);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/myassignments?id=" + id + "&submitted=1");
        } catch (IllegalArgumentException e) {
            request.setAttribute("formError", e.getMessage());
            request.setAttribute("formLink", link);
            showDetail(request, response, student, id);
        } catch (SQLException | IOException e) {
            LOG.log(Level.SEVERE, "Cannot save the submission of assignment " + id, e);
            request.setAttribute("formError", "Cannot save your submission. Please try again.");
            request.setAttribute("formLink", link);
            showDetail(request, response, student, id);
        }
    }

    // Shows "not found" when classId is null or the student is not enrolled in this class.
    private void showClassAssignments(HttpServletRequest request, HttpServletResponse response, User student,
            Integer classId) throws ServletException, IOException {
        Integer page = parseId(request.getParameter("page"));
        if (page == null) {
            page = 1;
        }
        try {
            ClassDTO classInfo = null;
            if (classId != null) {
                classInfo = service.getClass(student.getId(), classId);
            }
            if (classInfo == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("error", CLASS_NOT_FOUND);
            } else {
                request.setAttribute("classInfo", classInfo);
                request.setAttribute("result", service.getAssignmentPage(student.getId(), classId, page, PAGE_SIZE));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load the assignments of class " + classId, e);
            request.setAttribute("error", "Cannot load the assignments. Please try again later.");
        }
        forward(request, response, LIST_VIEW);
    }

    // Shows "not found" when id is null or the student cannot see this assignment.
    private void showDetail(HttpServletRequest request, HttpServletResponse response, User student, Integer id)
            throws ServletException, IOException {
        try {
            StudentAssignmentDTO row = null;
            if (id != null) {
                row = service.getAssignment(student.getId(), id);
            }
            if (row == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("error", NOT_FOUND);
            }
            request.setAttribute("row", row);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load assignment " + id, e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            request.setAttribute("error", "Cannot connect to the database.");
        }
        forward(request, response, DETAIL_VIEW);
    }

    // Returns the logged-in student. Otherwise redirects and returns null.
    private User currentStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return null;
        }
        try {
            if (!service.isStudent(user.getRoleId())) {
                request.getSession().setAttribute("error", "This page is for students only.");
                response.sendRedirect(request.getContextPath() + "/error.jsp");
                return null;
            }
            return user;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot read the user role", e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Cannot connect to the database.");
            return null;
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.setAttribute("fromServlet", Boolean.TRUE);
        request.getRequestDispatcher(view).forward(request, response);
    }

    // Positive integer, or null when the value is missing or invalid.
    private static Integer parseId(String value) {
        if (value == null) {
            return null;
        }
        try {
            int id = Integer.parseInt(value.trim());
            if (id > 0) {
                return id;
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
