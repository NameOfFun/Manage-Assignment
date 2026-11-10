package controller;

import dto.AssignmentDTO;
import dto.PageDTO;
import dto.SemesterOptionDTO;
import dto.SubmissionDTO;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Assignment;
import model.User;
import service.AssignmentService;
import service.SubmissionService;
import utils.SessionUtil;

/**
 * /assignment                     GET  → assignment list (+ filters and page)
 * /assignment?id=5                GET  → assignment details + group submissions
 * /assignment?action=edit&id=5    GET  → edit form
 * /assignment?action=new          GET  → create form
 * /assignment                     POST → create; action=update updates; action=grade grades a submission
 */
public class AssignmentServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AssignmentServlet.class.getName());
    private static final String LIST_VIEW = "/assignment/assignment.jsp";
    private static final String DETAILS_VIEW = "/assignment/assignmentdetails.jsp";
    private static final String EDIT_VIEW = "/assignment/editassignment.jsp";
    private static final String NEW_VIEW = "/assignment/newassignment.jsp";
    private static final int PAGE_SIZE = 10;
    private static final String INVALID_ID = "Invalid assignment ID. The ID must be a positive integer.";
    private static final String NO_ACCESS = "You do not have access to this assignment or it does not exist.";
    private final AssignmentService service = new AssignmentService();
    private SubmissionService submissionService;

    @Override
    public void init() {
        submissionService = SubmissionService.fromContext(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        String role = managerRole(user, request, response);
        if (role == null) {
            return;
        }

        String action = request.getParameter("action");
        if ("new".equals(action)) {
            showCreateForm(request, response, user, role);
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            showList(request, response, user, role);
            return;
        }

        Integer id = parseId(idParam);
        if (id == null) {
            showAccessError(request, response, HttpServletResponse.SC_BAD_REQUEST, INVALID_ID);
            return;
        }
        AssignmentDTO assignment = loadAssignment(id, user, role, request, response);
        if (assignment == null) {
            return;
        }

        if ("edit".equals(action)) {
            request.setAttribute("assignment", assignment);
            forward(request, response, EDIT_VIEW);
            return;
        }
        request.setAttribute("updated", "1".equals(request.getParameter("updated")));
        request.setAttribute("graded", "1".equals(request.getParameter("graded")));
        showDetails(request, response, assignment);
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
        String role = managerRole(user, request, response);
        if (role == null) {
            return;
        }

        String action = request.getParameter("action");
        if ("grade".equals(action)) {
            gradeSubmission(request, response, user, role);
        } else if ("update".equals(action)) {
            updateAssignment(request, response, user, role);
        } else {
            createAssignment(request, response, user, role);
        }
    }

    // ----- Pages -----

    // Filter dropdowns and the (filtered) assignment list.
    private void showList(HttpServletRequest request, HttpServletResponse response, User user, String role)
            throws ServletException, IOException {
        Integer subjectId = parseId(request.getParameter("subjectId"));
        Integer classId = parseId(request.getParameter("classId"));
        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        Integer page = parseId(request.getParameter("page"));
        if (page == null) {
            page = 1;
        }

        request.setAttribute("saved", "1".equals(request.getParameter("saved")));
        request.setAttribute("filterSubjectId", subjectId == null ? "" : subjectId);
        request.setAttribute("filterClassId", classId == null ? "" : classId);
        request.setAttribute("filterStatus", status == null ? "" : status);
        request.setAttribute("filterKeyword", keyword == null ? "" : keyword);

        boolean admin = isAdmin(role);
        try {
            SemesterOptionDTO term = termToShow(user, admin);
            request.setAttribute("classes", service.getClasses(user.getId(), admin, term));
            request.setAttribute("subjects", service.getSubjects(user.getId(), admin, term));
            request.setAttribute("result", service.getAssignmentPage(user.getId(), admin, term,
                    subjectId, classId, status, keyword, page, PAGE_SIZE));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load assignment data", e);
            request.setAttribute("classes", Collections.emptyList());
            request.setAttribute("subjects", Collections.emptyList());
            request.setAttribute("result", new PageDTO<AssignmentDTO>(Collections.emptyList(), 1, PAGE_SIZE, 0));
            request.setAttribute("error", "Cannot load assignments/classes from the database.");
        }
        forward(request, response, LIST_VIEW);
    }

    // Class and template dropdowns of the create page.
    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, User user, String role)
            throws ServletException, IOException {
        boolean admin = isAdmin(role);
        try {
            request.setAttribute("classes", service.getClasses(user.getId(), admin, termToShow(user, admin)));
            request.setAttribute("templates", service.getTemplates(user.getId(), admin));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load the create assignment form", e);
            request.setAttribute("classes", Collections.emptyList());
            request.setAttribute("templates", Collections.emptyList());
            request.setAttribute("error", "Cannot load classes/templates from the database.");
        }
        forward(request, response, NEW_VIEW);
    }

    // Details page with the class's groups and their submissions.
    private void showDetails(HttpServletRequest request, HttpServletResponse response, AssignmentDTO assignment)
            throws ServletException, IOException {
        request.setAttribute("assignment", assignment);
        try {
            List<SubmissionDTO> submissions = submissionService.getSubmissions(assignment.getAssignmentId());
            int submittedCount = 0;
            int gradedCount = 0;
            for (SubmissionDTO submission : submissions) {
                if (submission.isSubmitted()) {
                    submittedCount++;
                }
                if (submission.isGraded()) {
                    gradedCount++;
                }
            }
            request.setAttribute("submissions", submissions);
            request.setAttribute("submittedCount", submittedCount);
            request.setAttribute("gradedCount", gradedCount);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load submissions of assignment " + assignment.getAssignmentId(), e);
            request.setAttribute("submissions", Collections.emptyList());
            request.setAttribute("submissionError", "Cannot load the submissions.");
        }
        forward(request, response, DETAILS_VIEW);
    }

    // ----- Form actions -----

    private void createAssignment(HttpServletRequest request, HttpServletResponse response, User user, String role)
            throws ServletException, IOException {
        try {
            Assignment assignment = new Assignment();
            assignment.setClassId(Integer.parseInt(request.getParameter("classId")));
            String templateId = request.getParameter("templateId");
            if (templateId != null && !templateId.isBlank()) {
                assignment.setTemplateId(Integer.parseInt(templateId));
            }
            readFormFields(request, assignment);

            if (service.addAssignment(assignment, user.getId(), isAdmin(role)) == 0) {
                throw new IllegalArgumentException("The class does not exist or you are not assigned to it.");
            }
            response.sendRedirect(request.getContextPath() + "/assignment?saved=1");
        } catch (NumberFormatException e) {
            showCreateError(request, response, user, role, "Please choose a valid class.");
        } catch (IllegalArgumentException e) {
            showCreateError(request, response, user, role, e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot save assignment", e);
            showCreateError(request, response, user, role, "Cannot save the assignment. Please check the connection and the data.");
        }
    }

    private void updateAssignment(HttpServletRequest request, HttpServletResponse response, User user, String role)
            throws ServletException, IOException {
        Integer id = parseId(request.getParameter("id"));
        if (id == null) {
            showAccessError(request, response, HttpServletResponse.SC_BAD_REQUEST, INVALID_ID);
            return;
        }
        try {
            Assignment assignment = new Assignment();
            assignment.setAssignmentId(id);
            readFormFields(request, assignment);

            if (!service.updateAssignment(assignment, user.getId(), isAdmin(role))) {
                showAccessError(request, response, HttpServletResponse.SC_NOT_FOUND, NO_ACCESS);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/assignment?id=" + id + "&updated=1");
        } catch (IllegalArgumentException e) {
            showUpdateError(request, response, user, role, id, e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot save assignment", e);
            showUpdateError(request, response, user, role, id, "Cannot save the assignment. Please check the connection and the data.");
        }
    }

    // Score (0-10) and feedback for one submission, then back to the details page.
    private void gradeSubmission(HttpServletRequest request, HttpServletResponse response, User user, String role)
            throws ServletException, IOException {
        Integer id = parseId(request.getParameter("id"));
        Integer submissionId = parseId(request.getParameter("submissionId"));
        if (id == null || submissionId == null) {
            showAccessError(request, response, HttpServletResponse.SC_BAD_REQUEST, INVALID_ID);
            return;
        }
        AssignmentDTO assignment = loadAssignment(id, user, role, request, response);
        if (assignment == null) {
            return;
        }
        try {
            boolean graded = submissionService.grade(submissionId, id, request.getParameter("score"),
                    request.getParameter("feedback"), user.getId(), isAdmin(role));
            if (!graded) {
                throw new IllegalArgumentException("The submission does not exist or does not belong to this assignment.");
            }
            response.sendRedirect(request.getContextPath() + "/assignment?id=" + id + "&graded=1#submissions");
        } catch (IllegalArgumentException e) {
            request.setAttribute("gradeError", e.getMessage());
            request.setAttribute("gradeSubmissionId", submissionId);
            request.setAttribute("formScore", request.getParameter("score"));
            request.setAttribute("formFeedback", request.getParameter("feedback"));
            showDetails(request, response, assignment);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot grade submission " + submissionId, e);
            request.setAttribute("gradeError", "Cannot save the score. Please check the connection and the data.");
            request.setAttribute("gradeSubmissionId", submissionId);
            showDetails(request, response, assignment);
        }
    }

    // ----- Helpers -----

    // A teacher only sees the current term. An admin has no term of their own, so null means every term.
    private SemesterOptionDTO termToShow(User user, boolean admin) throws SQLException {
        if (admin) {
            return null;
        }
        return service.getCurrentTerm(user.getId());
    }

    // The fields that the create form and the edit form have in common.
    private void readFormFields(HttpServletRequest request, Assignment assignment) {
        assignment.setTitle(request.getParameter("title"));
        assignment.setInstruction(request.getParameter("instruction"));
        assignment.setStartDate(parseDate(request.getParameter("startDate")));
        assignment.setDueDate(parseDate(request.getParameter("dueDate")));
        assignment.setStatus(request.getParameter("status"));
    }

    // Shows the create page again with what the user typed and the error.
    private void showCreateError(HttpServletRequest request, HttpServletResponse response, User user, String role,
            String error) throws ServletException, IOException {
        keepFormInput(request);
        request.setAttribute("error", error);
        showCreateForm(request, response, user, role);
    }

    // Shows the edit page again with what the user typed and the error.
    private void showUpdateError(HttpServletRequest request, HttpServletResponse response, User user, String role,
            int id, String error) throws ServletException, IOException {
        keepFormInput(request);
        request.setAttribute("error", error);
        AssignmentDTO assignment = loadAssignment(id, user, role, request, response);
        if (assignment != null) {
            request.setAttribute("assignment", assignment);
            request.setAttribute("formSubmitted", Boolean.TRUE);
            forward(request, response, EDIT_VIEW);
        }
    }

    private void keepFormInput(HttpServletRequest request) {
        request.setAttribute("formTitle", request.getParameter("title"));
        request.setAttribute("formInstruction", request.getParameter("instruction"));
        request.setAttribute("formClassId", request.getParameter("classId"));
        request.setAttribute("formStartDate", request.getParameter("startDate"));
        request.setAttribute("formDueDate", request.getParameter("dueDate"));
        request.setAttribute("formStatus", request.getParameter("status"));
        request.setAttribute("formTemplateId", request.getParameter("templateId"));
    }

    // Returns "Admin" or "Teacher"; otherwise sends 403/503 and returns null.
    private String managerRole(User user, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String role = service.getManagerRoleName(user.getRoleId());
            if (role == null) {
                request.getSession().setAttribute("error", "The assignment management page is only for Teachers and Admins.");
                response.sendRedirect(request.getContextPath() + "/error.jsp");
                return null;
            }
            request.setAttribute("currentUser", user);
            request.setAttribute("managerRole", role);
            return role;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot verify assignment permissions", e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Cannot connect to the database.");
            return null;
        }
    }

    // Returns the assignment if this user may see it; otherwise shows the access error page and returns null.
    // A missing id and another teacher's assignment get the same message, so ids cannot be probed.
    private AssignmentDTO loadAssignment(int id, User user, String role, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            AssignmentDTO assignment = service.getAssignmentById(id, user.getId(), isAdmin(role));
            if (assignment == null) {
                showAccessError(request, response, HttpServletResponse.SC_NOT_FOUND, NO_ACCESS);
            }
            return assignment;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load assignment " + id, e);
            showAccessError(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Cannot connect to the database.");
            return null;
        }
    }

    // Keeps the header and sidebar: shows the message on the details page instead of Tomcat's error page.
    private void showAccessError(HttpServletRequest request, HttpServletResponse response, int status,
            String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("accessError", message);
        forward(request, response, DETAILS_VIEW);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.setAttribute("fromServlet", Boolean.TRUE);
        request.getRequestDispatcher(view).forward(request, response);
    }

    private static boolean isAdmin(String role) {
        return "Admin".equalsIgnoreCase(role);
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

    private static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Please enter the start date and the due date.");
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date/time.", e);
        }
    }
}
