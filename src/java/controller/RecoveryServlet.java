/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.api.services.gmail.Gmail;
import constants.Tokens;
import dto.UserDTO;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.GmailService;
import service.UserService;
import utils.VerificationUtil;

/**
 *
 * @author PC
 */
public class RecoveryServlet extends HttpServlet {

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");

		UserService userService = new UserService();

		String email = request.getParameter("email");

		UserDTO result;
		result = userService.getUserFromEmail(email);

		String action = request.getParameter("action");
		if (action == null || action.isEmpty()) {
			if (result.isSuccess()) {
				request.setAttribute("success", "Please check your mail!");
				sendRecoveryMail(Tokens.userAccessToken,
					email, result.getUser());
			} else {
				request.setAttribute("error", result.getMessage());
			}

			request.getRequestDispatcher("/forgotpassword.jsp").forward(request, response);
		} else if (action.equals("recoverpassword")) {
			HttpSession session = request.getSession();
			if (session == null) {
				return;
			}

			String username = (String) session.getAttribute("username_to_recover");

			String newPassword, confirmPassword;
			newPassword = request.getParameter("newPassword");
			confirmPassword = request.getParameter("confirmPassword");

			result = userService.getUserFromUsername(username);
			result = userService.recoverPassword(result.getUser(), newPassword, confirmPassword);

			if (result.isSuccess()) {
				request.setAttribute("success", "Password successfully updated!");
			} else {
				request.setAttribute("error", result.getMessage());
				request.getRequestDispatcher("/recoverpassword.jsp").forward(request, response);
				return;
			}

			// Remove the assigned username to recover
			session.invalidate();

			request.getRequestDispatcher("signin.jsp").forward(request, response);

		}

	}

	private void sendRecoveryMail(String userAccessToken, String recipientEmail, User user) {
		try {
			Gmail service = GmailService.getGmailService(userAccessToken);
			String link = VerificationUtil.generateVerificationLink("http://localhost:8080/AMS", "recover", user);

			MimeMessage rawMime = GmailService.createEmail(
				recipientEmail,
				"me", // or your actual email address
				"[AMS] Account recovery",
				String.format("""
						Username: %s
						Please recover your account password through this link, it will expire in 15 minutes: %s                                                 
					""", user.getUsername(), link)
			);

			// "me" tells Google to send as the authenticated OAuth account
			GmailService.sendEmail(service, "me", rawMime);

			System.out.println("Email sent successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
		processRequest(request, response);
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
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

}
