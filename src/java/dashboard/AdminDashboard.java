package dashboard;

import dto.AdminDashboardDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.AdminDashboardService;
import utils.SessionUtil;

public class AdminDashboard extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AdminDashboard.class.getName());
    private static final String VIEW = "/dashboard/admindashboard.jsp";

    private final AdminDashboardService service = new AdminDashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

        try {
            AdminDashboardDTO dash = service.getDashboard(sessionUser.getUsername());
            if (dash == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/signin.jsp");
                return;
            }
            request.setAttribute("dash", dash);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot load admin dashboard for " + sessionUser.getUsername(), e);
            request.setAttribute("error", "We could not load your dashboard right now. Please try again in a moment.");
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Admin Dashboard";
    }
}
