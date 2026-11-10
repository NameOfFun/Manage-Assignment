package dashboard;

import dto.StudentDashboardDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.StudentDashboardService;
import utils.SessionUtil;


public class StudentDashboard extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StudentDashboard.class.getName());
    private static final String VIEW = "/dashboard/studentdashboard.jsp";

    private final StudentDashboardService service = new StudentDashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null || sessionUser.getUsername() == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

        try {
            StudentDashboardDTO dash = service.getDashboard(sessionUser.getUsername());
            if (dash == null) {
                // The account was removed while the session was still alive
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/signin.jsp");
                return;
            }

            request.setAttribute("dash", dash);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load the student dashboard for " + sessionUser.getUsername(), e);
            request.setAttribute("error", "We could not load your dashboard right now. Please try again in a moment.");
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Student Dashboard";
    }
}
