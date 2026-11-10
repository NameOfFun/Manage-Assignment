/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dto.UserDTO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.UserService;

/**
 *
 * @author PC
 */
public class ProfileServlet extends HttpServlet {

	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute("user") != null) {
			UserService userService = new UserService();
			User user = (User) session.getAttribute("user");

			System.out.println(user.getImageUrl());
			
			session.setAttribute("roleName", userService.getUserRoleName(user));
			response.sendRedirect(request.getContextPath() + "/profile/userprofile.jsp");
			return;
		}

		request.getRequestDispatcher("signin.jsp").forward(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		UserService userService = new UserService();
		UserDTO result;

		String action = request.getParameter("action");
		if (action == null || action.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/signin.jsp");
			return;
		}

		HttpSession session = request.getSession();
		if (session != null && session.getAttribute("user") != null) {
			User user = (User) session.getAttribute("user");

			String password, newPassword, confirmPassword;
			password = request.getParameter("password");
			newPassword = request.getParameter("newPassword");
			confirmPassword = request.getParameter("confirmPassword");

			String username, fullName;
			username = request.getParameter("username");
			fullName = request.getParameter("fullName");

			if (action.contentEquals("update")) {
				result = userService.updateUser(user, username, fullName);
				if (result.isSuccess()) {
					request.setAttribute("success", "User fields successfully updated!");
				} else {
					request.setAttribute("error", result.getMessage());
				}

				request.getRequestDispatcher("/profile/userprofile.jsp").forward(request, response);
			} else if (action.contentEquals("changepassword")) {
				result = userService.updatePassword(user, password, newPassword, confirmPassword);
				if (result.isSuccess()) {
					request.setAttribute("success", "Password successfully updated!");
				} else {
					request.setAttribute("error", result.getMessage());
				}

				request.getRequestDispatcher("/profile/changepassword.jsp").forward(request, response);
			}
		} else {
			request.getRequestDispatcher("signin.jsp").forward(request, response);
		}
	}
}
