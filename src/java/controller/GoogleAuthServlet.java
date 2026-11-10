package controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import dto.UserDTO;
import jakarta.mail.internet.MimeMessage;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import service.UserService;
import service.GmailService;

import constants.Tokens;
import jakarta.servlet.http.HttpSession;
import model.User;
import utils.VerificationUtil;

public class GoogleAuthServlet extends HttpServlet {

	// Replace with your actual Client ID from Google Cloud Console
	private static final String CLIENT_ID = Tokens.clientId;

	private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
		new NetHttpTransport(),
		new GsonFactory()
	)
		.setAudience(Collections.singletonList(CLIENT_ID))
		.build();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String idTokenString = request.getParameter("id_token");

		if (idTokenString == null || idTokenString.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing ID token");
			return;
		}

		UserService userService = new UserService();
		UserDTO result;

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
			new NetHttpTransport(),
			GsonFactory.getDefaultInstance())
			.setAudience(Collections.singletonList(CLIENT_ID))
			.build();

		try {
			GoogleIdToken idToken = verifier.verify(idTokenString);
			if (idToken != null) {
				GoogleIdToken.Payload payload = idToken.getPayload();

				String email = payload.getEmail();
				String name = (String) payload.get("name");

				result = userService.getUserFromEmail(email);
				if (result.isSuccess()) {
					HttpSession session = request.getSession();
					session.setAttribute("user", result.getUser());
					session.setMaxInactiveInterval(30 * 60);
					
					response.sendRedirect(request.getContextPath() + "/dashboard");
				} else {
					result = userService.registerUserFromGoogle(email, name);
					if (result.isSuccess()) {
						sendVerificationMail(Tokens.userAccessToken,
							email, result.getUser());
					}

					response.sendRedirect("signin.jsp");
				}
			} else {
				// Invalid token
				response.sendRedirect("signin.jsp");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect("signin.jsp");
		}
	}

	private void sendVerificationMail(String userAccessToken, String recipientEmail, User user) {
		try {
			Gmail service = GmailService.getGmailService(userAccessToken);
			String link = VerificationUtil.generateVerificationLink("http://localhost:8080/AMS", "activate", user);

			MimeMessage rawMime = GmailService.createEmail(
				recipientEmail,
				"me", // or your actual email address
				"[AMS] Google account verification",
				String.format("""
						Username: %s
						Password: %s
						Please verify your account email through this link, it will expire in 15 minutes: %s                                                 
					""", user.getUsername(), user.getUsername(), link)
			);

			// "me" tells Google to send as the authenticated OAuth account
			GmailService.sendEmail(service, "me", rawMime);

			System.out.println("Email sent successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
