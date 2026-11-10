package service;

import dao.SettingDAO;
import dao.UserDAO;
import dto.UserDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Setting;
import model.User;
import utils.PasswordUtil;

public class UserService {

	private UserDAO udao;
	private SettingDAO settingDAO = new SettingDAO();

	public UserService() {
		try {
			udao = new UserDAO();
		} catch (Exception e) {
			System.err.println("Failed to initialize UserDAO: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public List<UserDTO> verifyUser(String username, String password) {
		List<UserDTO> messageList = new ArrayList<>();
		
		if (udao == null) {
			messageList.add(new UserDTO(false, "Database connection error.", null));
		}

		User u = udao.getUser(username);
		if (u == null) {
			messageList.add(new UserDTO(false, "Username not found!", null));
			return messageList;
		}

		if (u.getStatus().equals("Inactive")) {
			messageList.add(new UserDTO(false, "This account is inactive!", null));
		}

		String hash = u.getPasswordHash();
		if (!PasswordUtil.verifyPassword(password, hash)) {
			messageList.add(new UserDTO(false, "Password not found!", null));
		}

		// Contains errors
		if(!messageList.isEmpty()) {
			return messageList;
		}
		
		messageList.add(new UserDTO(true, "User verified!", u));
		return messageList;
	}

	public List<UserDTO> registerUser(String username, String password, String confirmPassword,
		String email, String fullName) {
		
		List<UserDTO> messageList = new ArrayList<>();
		
		if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
			|| email.isEmpty() || fullName.isEmpty()) {
			messageList.add(new UserDTO(false, "Fields cannot be empty.", null));
			return messageList;
		}

		username = username.trim();
		if (username.length() < 3 || username.length() > 50) {
			messageList.add(new UserDTO(false, "Username must be between 3 and 50 characters.", null));
		}

		if (password.length() < 8 || password.length() > 50) {
			messageList.add(new UserDTO(false, "Password must be between 8 and 50 characters.", null));
		}
		
		if (!password.equals(confirmPassword)) {
			messageList.add(new UserDTO(false, "Confirm password and password do not match!", null));
		}


		email = email.trim();
		if (email.length() > 50) {
			messageList.add(new UserDTO(false, "Email cannot exceed 50 characters.", null));
		} else if (!email.contains("@") || !email.contains(".")) {
			messageList.add(new UserDTO(false, "Email uses invalid format!", null));			
		}
		User u = udao.getUserFromEmail(email);
		if(u != null) {
			messageList.add(new UserDTO(false, "Email already exists!", null));
		}

		fullName = fullName.trim();
		if (fullName.length() > 50) {
			messageList.add(new UserDTO(false, "Full name cannot exceed 50 characters.", null));
		}

		if (udao == null) {
			messageList.add(new UserDTO(false, "Database connection error.", null));
		}
		u = udao.getUser(username);
		if (u != null) {
			messageList.add(new UserDTO(false, "Username already exists!", null));
		}
		
		// Contains errors
		if(!messageList.isEmpty()) {
			return messageList;
		}

		// ID 2 = student role
		User x = new User(2, username, PasswordUtil.hashPassword(password));
		x.setEmail(email);
		x.setFullName(fullName);
		x.setStatus("Inactive");
		x.setCreatedAt(LocalDate.now());
		udao.addUser(x);
		
		// Add success message
		messageList.add(new UserDTO(true, "Account created!", x));
		return messageList;
	}

	// For Google registration
	public UserDTO registerUserFromGoogle(String email, String name) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error.", null);
		}
		User existingUser = udao.getUserFromEmail(email);
		if (existingUser != null) {
			return new UserDTO(false, "The email is already connected to an account!", existingUser);
		}

		// Generate unique name
		String baseUsername = email.split("@")[0];
		String uniqueUsername = generateUniqueUsername(baseUsername);

		// Construct new user
		User newUser = new User(2, uniqueUsername, PasswordUtil.hashPassword(uniqueUsername));
		newUser.setEmail(email);
		if (name != null) {
			newUser.setFullName(name);
		}
		newUser.setStatus("Inactive");
		newUser.setCreatedAt(LocalDate.now());

		udao.addUser(newUser);

		return new UserDTO(true, "Please check your mail for the link we sent!", newUser);
	}

	public UserDTO updateUser(User user, String username, String fullName) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}
		User u = udao.getUser(user.getUsername());
		if (u == null || u.getId() != user.getId()) {
			return new UserDTO(false, "User doesn't exist!", null);
		}

		if (username.isEmpty() || fullName.isEmpty()) {
			return new UserDTO(false, "User fields cannot be empty.", null);
		}
		
		username = username.trim();
		u = udao.getUser(username);
		if(u != null && u.getId() != user.getId()) {
			return new UserDTO(false, "This username is already used!", null);
		}
		if(username.length() < 3 || username.length() > 50) {
			return new UserDTO(false, "Username must be between 3 and 50 characters!", null);			
		}

		fullName = fullName.trim();
		if (fullName.length() > 50) {
			return new UserDTO(false, "Full name cannot exceed 50 characters.", null);
		}

		user.setUsername(username);
		user.setFullName(fullName);

		udao.updateUser(user);

		return new UserDTO(true, "success", user);
	}

	public UserDTO updatePassword(User user, String password, String newPassword, String confirmPassword) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}
		User u = udao.getUser(user.getUsername());
		if (u == null || u.getId() != user.getId()) {
			return new UserDTO(false, "User doesn't exist!", null);
		}

		if (password.isEmpty() || newPassword.isEmpty()) {
			return new UserDTO(false, "Password fields cannot be empty.", null);
		}

		if (!PasswordUtil.verifyPassword(password, u.getPasswordHash())) {
			return new UserDTO(false, "Current password does not match!", null);
		}

		if (newPassword.length() > 50 || newPassword.length() < 8) {
			return new UserDTO(false, "New password must be between 8 and 50 characters.", null);

		}

		if (!newPassword.equals(confirmPassword)) {
			return new UserDTO(false, "Confirm password and new password do not match!", null);
		}

		user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
		udao.updateUser(user);

		return new UserDTO(true, "success", user);
	}

	public UserDTO recoverPassword(User user, String newPassword, String confirmPassword) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}
		User u = udao.getUser(user.getUsername());
		if (u == null || u.getId() != user.getId()) {
			return new UserDTO(false, "User doesn't exist!", null);
		}

		if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
			return new UserDTO(false, "Password fields cannot be empty.", null);
		}

		if (newPassword.length() > 50 || newPassword.length() < 8) {
			return new UserDTO(false, "New password must be between 8 and 50 characters.", null);

		}

		if (!newPassword.equals(confirmPassword)) {
			return new UserDTO(false, "Confirm password and new password do not match!", null);
		}

		user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
		udao.updateUser(user);

		return new UserDTO(true, "success", user);
	}

	public UserDTO updateUserStatus(User user, String status) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}
		User u = udao.getUser(user.getUsername());
		if (u == null || u.getId() != user.getId()) {
			return new UserDTO(false, "User doesn't exist!", null);
		}

		if (status.isEmpty()) {
			return new UserDTO(false, "Status cannot be empty.", null);
		}

		user.setStatus(status);
		udao.updateUser(user);

		return new UserDTO(true, "Account status updated!", user);
	}

	public UserDTO updateUserProfile(User user, String imageUrl) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}
		User u = udao.getUser(user.getUsername());
		if (u == null || u.getId() != user.getId()) {
			return new UserDTO(false, "User doesn't exist!", null);
		}

		if (imageUrl.isEmpty()) {
			return new UserDTO(false, "Image URL cannot be empty.", null);
		}

		user.setImageUrl(imageUrl);
		udao.updateUser(user);

		return new UserDTO(true, "Account status updated!", user);
	}

	
	public String getUserRoleName(User u) {
		if (udao == null) {
			return null;
		}
		return udao.getRoleName(u.getRoleId());
	}

	public UserDTO getUserFromEmail(String email) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}

		if (email == null || email.isEmpty()) {
			return new UserDTO(false, "Email cannot be empty!", null);
		}

		User u = udao.getUserFromEmail(email);
		if (u == null) {
			return new UserDTO(false, "The email isn't connected to any account!", null);
		}

		return new UserDTO(true, "success", u);
	}

	public UserDTO getUserFromUsername(String username) {
		if (udao == null) {
			return new UserDTO(false, "Database connection error", null);
		}

		if (username == null || username.isEmpty()) {
			return new UserDTO(false, "Username cannot be empty!", null);
		}

		User u = udao.getUser(username);
		if (u == null) {
			return new UserDTO(false, "The username isn't connected to any account!", null);
		}

		return new UserDTO(true, "success", u);
	}

	// Helper to guarantee unique username
	private String generateUniqueUsername(String base) {
		if (udao == null) {
			return base;
		}
		String username = base;
		while (udao.getUser(username) != null) {
			int randomSuffix = (int) (Math.random() * 9000) + 1000;
			username = base + randomSuffix;
		}
		return username;
	}

	// -------------------------------------------------------------------------
	// User Management (Admin)
	// -------------------------------------------------------------------------

	// Tìm kiếm user có filter + sort + phân trang
	public List<User> searchUsers(Integer roleId, String status, String keyword, String sortBy, String sortDir, int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		return udao.searchUsers(roleId, status, keyword, sortBy, sortDir, pageSize, offset);
	}

	public int countSearchUsers(Integer roleId, String status, String keyword) {
		return udao.countSearchUsers(roleId, status, keyword);
	}

	// Lấy user theo id
	public UserDTO getUserById(int userId) {
		User u = udao.getUserById(userId);
		if (u == null) {
			return new UserDTO(false, "User not found.", null);
		}
		return new UserDTO(true, null, u);
	}

	// Tạo user mới (Admin)
	public UserDTO createUser(User u) {
		if (udao.getUser(u.getUsername()) != null) {
			return new UserDTO(false, "This username is already taken. Please choose another one.", null);
		}
		udao.addUser(u);
		return new UserDTO(true, "User \"" + u.getUsername() + "\" has been created successfully.", u);
	}

	// Cập nhật user (Admin)
	public UserDTO updateUser(User u) {
		udao.updateUser(u);
		return new UserDTO(true, "User \"" + u.getUsername() + "\" has been updated successfully.", u);
	}

	// Cập nhật status (Activate/Deactivate)
	public void updateUserStatus(int userId, String status) {
		udao.updateUserStatus(userId, status);
	}

	// Lấy danh sách roles từ DB
	public List<Setting> getRoles() {
		return settingDAO.getByType("Role");
	}

	// Lấy subjects đang assign cho expert
	public List<Setting> getSubjectsByExpert(int expertId) {
		return udao.getSubjectsByExpert(expertId);
	}

	// Cập nhật danh sách subject của expert
	public void replaceSubjectExpert(int expertId, java.util.List<Integer> subjectIds) {
		udao.replaceSubjectExpert(expertId, subjectIds);
	}
}
