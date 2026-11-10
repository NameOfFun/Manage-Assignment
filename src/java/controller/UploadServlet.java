package controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dto.UserDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import model.User;
import service.UserService;
import utils.CloudinaryUtil;

@MultipartConfig(
	fileSizeThreshold = 1024 * 1024 * 2, // 2MB
	maxFileSize = 1024 * 1024 * 5, // 5MB
	maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class UploadServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		HttpSession session = request.getSession();
		if (session != null && session.getAttribute("user") != null) {
			User user = (User) session.getAttribute("user");
			UserService userService = new UserService();
			UserDTO result;

			Part filePart = request.getPart("image");
			if (filePart == null || filePart.getSize() == 0) {
				request.setAttribute("error", "No image file found!");
				request.getRequestDispatcher("/profile/userprofile.jsp").forward(request, response);
				return;
			}

			String contentType = filePart.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				request.setAttribute("error", "Only image files (JPG, PNG, GIF, WebP, etc.) are allowed!");
				request.getRequestDispatcher("/profile/userprofile.jsp").forward(request, response);
				return;
			}

			if (filePart.getSize() > 5 * 1024 * 1024) { // 5,242,880 bytes
				request.setAttribute("error", "Image file cannot exceed 5 MB!");
				request.getRequestDispatcher("/profile/userprofile.jsp").forward(request, response);
				return;
			}

			// Read input stream directly from memory/temp space
			byte[] fileBytes;
			try (InputStream inputStream = filePart.getInputStream()) {
				fileBytes = inputStream.readAllBytes();
			}

			// Upload to Cloudinary
			Cloudinary cloudinary = CloudinaryUtil.getInstance();
			Map uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.emptyMap());

			String imageUrl = (String) uploadResult.get("secure_url");
			result = userService.updateUserProfile(user, imageUrl);
			if (result.isSuccess()) {
				request.setAttribute("success", "User profile successfully updated!");
			} else {
				request.setAttribute("error", result.getMessage());
			}

			request.getRequestDispatcher("/profile/userprofile.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher("signin.jsp").forward(request, response);
		}

	}
}
