/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/AdvancedFilter.java to edit this template
 */
package filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.UserService;

/**
 *
 * @author PC
 */
@WebFilter(urlPatterns = {"/admindashboard/*", "/studentdashboard/*", "/teacherdashboard/*", "/expertdashboard/*",
	"/dashboard/*", "/profile/*", "/admin/*", "/class/*"})
public class AuthFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		HttpSession session = req.getSession(false);
		User user;
		if (session != null) {
			user = (User) session.getAttribute("user");
		} else {
			resp.sendRedirect(req.getContextPath() + "/signin.jsp");
			return;
		}

		if (user == null) {
			resp.sendRedirect(req.getContextPath() + "/signin.jsp");
			return;
		}

		UserService userService = new UserService();
		String roleName = userService.getUserRoleName(user);

		// Safely handle null role names
		if (roleName == null) {
			roleName = "Student";
		}

		String requestURI = req.getRequestURI();
		String contextPath = req.getContextPath();

		// Dashboard folder and not a specific dashboard route
		if (requestURI.contains("/dashboard")) {
			// Determine target dashboard based on role
			String targetDashboard;
			switch (roleName) {
				case "Admin":
					targetDashboard = contextPath + "/admindashboard";
					break;
				case "Teacher":
					targetDashboard = contextPath + "/teacherdashboard";
					break;
				case "Expert":
					targetDashboard = contextPath + "/expertdashboard";
					break;
				case "Student":
				default:
					targetDashboard = contextPath + "/studentdashboard";
					break;
			}

			// Prevent infinite redirect loop:
			// Only redirect if the user is NOT already on their assigned dashboard page
			if (!requestURI.equals(targetDashboard)) {
				resp.sendRedirect(targetDashboard);
				return;
			}
		} else if (requestURI.contains("admindashboard") || requestURI.contains("admin")) {
			if (!roleName.equals("Admin")) {
				session.setAttribute("error", "This page is for admins only.");
				request.getRequestDispatcher("/error.jsp").forward(request, response);
				return;
			}
		} else if (requestURI.contains("teacherdashboard")) {
			if (!roleName.equals("Teacher")) {
				session.setAttribute("error", "This page is for teachers only.");
				request.getRequestDispatcher("/error.jsp").forward(request, response);
				return;
			}
		} else if (requestURI.contains("expertdashboard")) {
			if (!roleName.equals("Expert")) {
				session.setAttribute("error", "This page is for experts only.");
				request.getRequestDispatcher("/error.jsp").forward(request, response);
				return;
			}
		} else if (requestURI.contains("studentdashboard")) {
			if (!roleName.equals("Student")) {
				session.setAttribute("error", "This page is for students only.");
				request.getRequestDispatcher("/error.jsp").forward(request, response);
				return;
			}
		}

		// User is already on the correct dashboard page -> allow request through
		chain.doFilter(request, response);
	}
}
