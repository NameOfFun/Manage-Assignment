package controller;

import dto.PageDTO;
import dto.SemesterOptionDTO;
import dto.SubmissionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.AssignmentService;
import service.SubmissionService;
import utils.SessionUtil;

/**
 * /submissions                     GET  → Submission List of the teacher's classes in the current term (all classes for admins)
 *                                         filters: classId, status (pending|graded|late), keyword, page
 * /submissions/detail?id=7         GET  → Submission Details (teacher of the class / admin)
 * /submissions/detail              POST → grade (teacher of the class / admin): id, score, feedback
 */
public class SubmissionServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SubmissionServlet.class.getName());
    private static final String DETAIL_PATH = "/submissions/detail";
    private static final String LIST_VIEW = "/submission/submissionlist.jsp";
    private static final String DETAIL_VIEW = "/submission/submissiondetail.jsp";
    private static final int PAGE_SIZE = 10;
    private static final int KEYWORD_MAX = 100;
    private static final List<String> STATUSES = List.of("pending", "graded", "late");
    private static final String NO_ACCESS = "The submission does not exist or you do not have permission to view it.";

    private final AssignmentService assignmentService = new AssignmentService();
    private SubmissionService service;

    @Override
    public void init() {
        service = SubmissionService.fromContext(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        String role = managerRoleOrNull(user, request, response);
        if (role == null) {
            return;
        }

        if (DETAIL_PATH.equals(request.getServletPath())) {
            Integer id = parseId(request.getParameter("id"));
            if (id == null) {
                showAccessError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid submission ID. The ID must be a positive integer.");
                return;
            }
            request.setAttribute("graded", "1".equals(request.getParameter("graded")));
            showDetail(request, response, user, role, id);
            return;
        }

        showList(request, response, user, role);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        String role = managerRoleOrNull(user, request, response);
        if (role == null) {
            return;
        }
        Integer id = parseId(request.getParameter("id"));
        if (id == null || !DETAIL_PATH.equals(request.getServletPath())) {
            showAccessError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid submission ID. The ID must be a positive integer.");
            return;
        }

        try {
            SubmissionDTO submission = service.getSubmissionDetail(id, user.getId(), isAdmin(role));
            if (submission == null || !canGrade(submission, user, role)) {
                showAccessError(request, response, HttpServletResponse.SC_FORBIDDEN,
                        "Only the class teacher or an Admin can grade this submission.");
                return;
            }
            if (!service.grade(id, submission.getAssignmentId(), request.getParameter("score"),
                    request.getParameter("feedback"), user.getId(), isAdmin(role))) {
                throw new IllegalArgumentException(NO_ACCESS);
            }
            response.sendRedirect(request.getContextPath() + DETAIL_PATH + "?id=" + id + "&graded=1");
        } catch (IllegalArgumentException e) {
            request.setAttribute("gradeError", e.getMessage());
            request.setAttribute("formScore", request.getParameter("score"));
            request.setAttribute("formFeedback", request.getParameter("feedback"));
            showDetail(request, response, user, role, id);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot grade submission " + id, e);
            request.setAttribute("gradeError", "Cannot save the score. Please check the connection and the data.");
            showDetail(request, response, user, role, id);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, User user, String role)
            throws ServletException, IOException {
        Integer classId = parseId(request.getParameter("classId"));
        String status = request.getParameter("status");
        if (status == null || !STATUSES.contains(status)) {
            status = "";
        }
        String keyword = request.getParameter("keyword");
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();
        if (keyword.length() > KEYWORD_MAX) {
            keyword = keyword.substring(0, KEYWORD_MAX);
        }
        Integer page = parseId(request.getParameter("page"));
        if (page == null) {
            page = 1;
        }

        request.setAttribute("filterClassId", classId == null ? "" : classId);
        request.setAttribute("filterStatus", status);
        request.setAttribute("filterKeyword", keyword);
        request.setAttribute("filtered", classId != null || !status.isEmpty() || !keyword.isEmpty());

        boolean admin = isAdmin(role);
        try {
            // A teacher only sees the current term. An admin has no term of their own, so term stays null (every term).
            SemesterOptionDTO term = null;
            if (!admin) {
                term = assignmentService.getCurrentTerm(user.getId());
            }
            request.setAttribute("classes", assignmentService.getClasses(user.getId(), admin, term));
            request.setAttribute("result", service.getSubmissionPage(user.getId(), admin, term,
                    classId, status, keyword, page, PAGE_SIZE));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load submission list", e);
            request.setAttribute("classes", Collections.emptyList());
            request.setAttribute("result", new PageDTO<SubmissionDTO>(Collections.emptyList(), 1, PAGE_SIZE, 0));
            request.setAttribute("error", "Cannot load the submissions. Please try again later.");
        }
        forward(request, response, LIST_VIEW);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, User user, String role,
            int id) throws ServletException, IOException {
        try {
            SubmissionDTO submission = service.getSubmissionDetail(id, user.getId(), isAdmin(role));
            if (submission == null) {
                showAccessError(request, response, HttpServletResponse.SC_NOT_FOUND, NO_ACCESS);
                return;
            }
            request.setAttribute("submission", submission);
            request.setAttribute("canGrade", canGrade(submission, user, role));
            forward(request, response, DETAIL_VIEW);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load submission " + id, e);
            showAccessError(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Cannot connect to the database.");
        }
    }

    /** "Teacher" or "Admin"; other roles go to the error page and get null (503 when the database is down). */
    private String managerRoleOrNull(User user, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String role = assignmentService.getManagerRoleName(user.getRoleId());
            if (role == null) {
                request.getSession().setAttribute("error", "The submissions page is only for Teachers and Admins.");
                response.sendRedirect(request.getContextPath() + "/error.jsp");
            }
            return role;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot read the user role", e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Cannot connect to the database.");
            return null;
        }
    }

    private static boolean canGrade(SubmissionDTO submission, User user, String role) {
        return isAdmin(role) || ("Teacher".equalsIgnoreCase(role) && submission.getClassTeacherId() == user.getId());
    }

    private static boolean isAdmin(String role) {
        return "Admin".equalsIgnoreCase(role);
    }

    private void showAccessError(HttpServletRequest request, HttpServletResponse response, int status,
            String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("accessError", message);
        forward(request, response, DETAIL_VIEW);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.setAttribute("fromServlet", Boolean.TRUE);
        request.getRequestDispatcher(view).forward(request, response);
    }

    private static Integer parseId(String value) {
        try {
            int id = Integer.parseInt(value == null ? "" : value.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
