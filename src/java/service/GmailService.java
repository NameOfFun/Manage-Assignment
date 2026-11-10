package service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

public class GmailService {

	public static Gmail getGmailService(String accessToken) throws Exception {
		// Build a Credential using the Bearer Token method
		Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
			.setAccessToken(accessToken);

		return new Gmail.Builder(
			GoogleNetHttpTransport.newTrustedTransport(),
			GsonFactory.getDefaultInstance(),
			credential
		)
			.setApplicationName("Assignment Management System")
			.build();
	}

	public static MimeMessage createEmail(String toEmail, String fromEmail, String subject, String bodyText)
		throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);
		email.setFrom(new InternetAddress(fromEmail));
		email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
		email.setSubject(subject);
		email.setText(bodyText);
		return email;
	}

	public static Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] rawMessageBytes = buffer.toByteArray();

		// Gmail API requires URL-safe Base64 encoding
		String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	public static void sendEmail(Gmail service, String userId, MimeMessage emailContent)
		throws MessagingException, IOException {
		Message message = createMessageWithEmail(emailContent);
		// userId can be "me" for the authenticated OAuth user
		service.users().messages().send(userId, message).execute();
	}
}
