package utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import constants.Tokens;
import model.User;

public class VerificationUtil {

	private static final String HMAC_SECRET = Tokens.HMAC_SECRET;
	private static final long EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

	public static String generateVerificationLink(String baseUrl, String action, User user) {
		String cleanUsername = user.getUsername().trim().toLowerCase();
		long expiresAt = System.currentTimeMillis() + EXPIRATION_MS;
		String payload = cleanUsername + ":" + expiresAt + ":" + user.getPasswordHash();

		String signature = hmacSha256(payload, HMAC_SECRET);

		return baseUrl + "/verify?action=" + action + "&p=" + payload + "&sig=" + signature;
	}

	public static String validateTokenAndGetUsername(String payload, String signature) {
		if (payload == null || signature == null || !payload.contains(":")) {
			return null;
		}

		// Re-compute HMAC to check for tampering
		String expectedSig = hmacSha256(payload, HMAC_SECRET);
		if (!expectedSig.equals(signature)) {
			return null; // Tampered payload or invalid signature
		}

		// Check expiration timestamp
		String[] parts = payload.split(":");
		String username = parts[0];
		long expiresAt = Long.parseLong(parts[1]);

		if (System.currentTimeMillis() > expiresAt) {
			return null; // Token expired
		}

		return username;
	}

	public static String validateTokenAndPasswordHash(String payload, String signature) {
		if (payload == null || signature == null || !payload.contains(":")) {
			return null;
		}

		// Re-compute HMAC to check for tampering
		String expectedSig = hmacSha256(payload, HMAC_SECRET);
		if (!expectedSig.equals(signature)) {
			return null; // Tampered payload or invalid signature
		}

		// Check expiration timestamp
		String[] parts = payload.split(":");
		String passwordHash = parts[2];
		long expiresAt = Long.parseLong(parts[1]);

		if (System.currentTimeMillis() > expiresAt) {
			return null; // Token expired
		}

		return passwordHash;
	}

	
	// HMAC SHA 256 calculation
	private static String hmacSha256(String data, String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			mac.init(secretKey);
			byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("Error generating HMAC-SHA256 signature", e);
		}
	}
}
