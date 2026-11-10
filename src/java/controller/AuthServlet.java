/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.api.services.gmail.Gmail;
import constants.Tokens;
import dto.UserDTO;
import jakarta.mail.internet.MimeMessage;
import service.UserService;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.User;
import service.GmailService;
import utils.VerificationUtil;

/**
 *
 * @author PC
 */
public class AuthServlet extends HttpServlet {

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
			response.sendRedirect(request.getContextPath() + "/dashboard");
			return;
		}

		// Handle non-authenticated routes
		String action = request.getParameter("action");
		if (action == null || action.equals("signin")) {
			request.getRequestDispatcher("signin.jsp").forward(request, response);
			return;
		}

		if (action.equals("register")) {
			request.getRequestDispatcher("register.jsp").forward(request, response);
		}
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

		String username, password;
		username = request.getParameter("username");
		password = request.getParameter("password");

		List<UserDTO> resultList;

		String action = request.getParameter("action");
		if (action == null || action.isEmpty()) {
			response.sendRedirect("signin.jsp");
			return;
		} else if (action.equals("logout")) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			response.sendRedirect("signin.jsp");
			return;
		}

		String targetJsp = action.equals("signin") ? "signin.jsp" : "register.jsp";

		if (action.contentEquals("register")) {
			String email = request.getParameter("email");
			String fullName = request.getParameter("fullName");
			String confirmPassword = request.getParameter("confirmPassword");
			resultList = userService.registerUser(username, password, confirmPassword, email, fullName);

			if (resultList.size() == 1 && resultList.get(0).isSuccess()) {
				sendVerificationMail(Tokens.userAccessToken,
					email, resultList.get(0).getUser());

				response.sendRedirect("signin.jsp");
				return;
			}
		} else {
			resultList = userService.verifyUser(username, password);
			if (resultList.size() == 1 && resultList.get(0).isSuccess()) {
				HttpSession session = request.getSession();
				session.setAttribute("user", resultList.get(0).getUser());
				session.setMaxInactiveInterval(30 * 60);

				response.sendRedirect(request.getContextPath() + "/dashboard");
				return;
			}
		}

		request.setAttribute("messages", resultList);
		request.getRequestDispatcher(targetJsp).forward(request, response);
	}

	private void sendVerificationMail(String userAccessToken, String recipientEmail, User user) {
		try {
			Gmail service = GmailService.getGmailService(userAccessToken);
			String link = VerificationUtil.generateVerificationLink("http://localhost:8080/AMS", "activate", user);

			MimeMessage rawMime = GmailService.createEmail(
				recipientEmail,
				"me", // or your actual email address
				"[AMS] Account verification",
				String.format("""
						Username: %s
						Please verify your account email through this link, it will expire in 15 minutes: %s                                                 
					""", user.getUsername(), link)
			);

			// "me" tells Google to send as the authenticated OAuth account
			GmailService.sendEmail(service, "me", rawMime);

			System.out.println("Email sent successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
