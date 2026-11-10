package dashboard;

import dto.AssignmentDTO;
import dto.SemesterOptionDTO;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Class;
import model.User;
import service.AssignmentService;
import utils.SessionUtil;

// Login and the Teacher role are already checked by AuthFilter ("/teacherdashboard/*").
public class TeacherDashboard extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TeacherDashboard.class.getName());
    private static final String VIEW = "/dashboard/teacherdashboard.jsp";
    private static final int RECENT_ASSIGNMENTS = 5;
    private final AssignmentService service = new AssignmentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User teacher = SessionUtil.validateSession(request, response);
        if (teacher == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        int teacherId = teacher.getId();

        // Stay empty if the database fails, so the page can still show the error
        String currentTerm = "";
        List<Class> classes = new ArrayList<>();
        int assignmentCount = 0;
        List<AssignmentDTO> recentAssignments = new ArrayList<>();
        List<AssignmentDTO> reviews = new ArrayList<>();
        try {
            SemesterOptionDTO term = service.getCurrentTerm(teacherId);
            if (term != null) {
                currentTerm = term.getSemester() + " " + term.getYear();
            }
            classes = service.getClasses(teacherId, false, term);
            assignmentCount = service.countTeacherAssignments(teacherId, term);
            recentAssignments = service.getRecentAssignments(teacherId, RECENT_ASSIGNMENTS);
            reviews = service.getAssignmentsToEvaluate(teacherId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load teacher dashboard", e);
            request.setAttribute("error", "Cannot load the dashboard data from the database.");
        }

        int pendingReviewCount = 0;
        for (AssignmentDTO review : reviews) {
            pendingReviewCount += review.getPendingSubmissionCount();
        }

        request.setAttribute("currentTerm", currentTerm);
        request.setAttribute("teacherClasses", classes);
        request.setAttribute("classCount", classes.size());
        request.setAttribute("assignmentCount", assignmentCount);
        request.setAttribute("recentAssignments", recentAssignments);
        request.setAttribute("reviewAssignments", reviews);
        request.setAttribute("pendingReviewCount", pendingReviewCount);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
