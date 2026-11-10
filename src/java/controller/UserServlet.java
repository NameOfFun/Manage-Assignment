package controller;

import com.google.api.services.gmail.Gmail;
import constants.Tokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import dto.UserDTO;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import model.Setting;
import model.User;
import service.UserService;
import service.GmailService;
import service.SettingService;
import utils.SessionUtil;

public class UserServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UserServlet.class.getName());
    private static final String USER_LIST_VIEW = "/admin/userlist.jsp";

    private final UserService service = new UserService();
    private final SettingService settingService = new SettingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("activate".equals(action) || "deactivate".equals(action)) {
            handleStatusChange(request, response, action);

        } else if ("new".equals(action)) {
            request.setAttribute("roles", service.getRoles());
            request.getRequestDispatcher("/admin/newuser.jsp").forward(request, response);

        } else if ("edit".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr != null) {
                UserDTO dto = service.getUserById(Integer.parseInt(idStr));
                if (dto.isSuccess()) {
                    User u = dto.getUser();
                    request.setAttribute("editUser", u);
                    // Format createdAt: dd-MM-yyyy
                    if (u.getCreatedAt() != null) {
                        String formatted = String.format("%02d-%02d-%04d",
                            u.getCreatedAt().getDayOfMonth(),
                            u.getCreatedAt().getMonthValue(),
                            u.getCreatedAt().getYear());
                        request.setAttribute("createdAtFormatted", formatted);
                    }
                    // Nếu là Expert thì load thêm subjects
                    String roleName = "";
                    for (Setting r : service.getRoles()) {
                        if (r.getId() == u.getRoleId()) { roleName = r.getName(); break; }
                    }
                    if ("Expert".equalsIgnoreCase(roleName)) {
                        request.setAttribute("expertSubjects", service.getSubjectsByExpert(u.getId()));
                        request.setAttribute("allSubjects", settingService.getByType("Subject"));
                    }
                }
            }
            request.setAttribute("roles", service.getRoles());
            request.getRequestDispatcher("/admin/userdetail.jsp").forward(request, response);

        } else {
            showUserList(request, response);
        }
    }

    private static final int PAGE_SIZE = 10;

    private void showUserList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String roleIdStr = request.getParameter("roleId");
            String status    = request.getParameter("status");
            String keyword   = request.getParameter("keyword");
            String pageStr   = request.getParameter("page");
            String sortBy    = request.getParameter("sortBy");
            String sortDir   = request.getParameter("sortDir");

            Integer roleId = (roleIdStr != null && !roleIdStr.isEmpty()) ? Integer.valueOf(roleIdStr) : null;
            int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
            if (page < 1) page = 1;
            if (sortBy == null || sortBy.isEmpty()) sortBy = "fullName";
            if (sortDir == null || sortDir.isEmpty()) sortDir = "asc";

            // Tất cả filter + search + sort + paging đều thực hiện ở DB
            int totalUsers = service.countSearchUsers(roleId, status, keyword);
            List<User> users = service.searchUsers(roleId, status, keyword, sortBy, sortDir, page, PAGE_SIZE);

            int totalPages = (int) Math.ceil((double) totalUsers / PAGE_SIZE);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("users", users);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("sortBy", sortBy);
            request.setAttribute("sortDir", sortDir);
            request.setAttribute("roles", service.getRoles());

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot load user list", e);
            request.setAttribute("error", "Could not load user list. Please try again.");
        }

        request.getRequestDispatcher(USER_LIST_VIEW).forward(request, response);
    }

    private void handleStatusChange(HttpServletRequest request, HttpServletResponse response, String action)
            throws IOException {
        try {
            int userId = Integer.parseInt(request.getParameter("id"));
            String newStatus = "activate".equals(action) ? "Active" : "Inactive";
            service.updateUserStatus(userId, newStatus);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot update user status", e);
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User sessionUser = SessionUtil.validateSession(request, response);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }

	String action = request.getParameter("action");
        if ("create".equals(action)) {
            handleCreateUser(request, response);
        } else if ("update".equals(action)) {
            handleUpdateUser(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String rawFullName = request.getParameter("fullName");
        String rawUsername = request.getParameter("username");
        String rawEmail    = request.getParameter("email");
        String status      = request.getParameter("status");
        String roleIdStr   = request.getParameter("roleId");

        String fullName = (rawFullName != null) ? rawFullName.trim() : "";
        String username = (rawUsername != null) ? rawUsername.trim() : "";
        String email    = (rawEmail != null)    ? rawEmail.trim().toLowerCase()    : "";
        status          = (status != null && !status.trim().isEmpty()) ? status.trim() : "Active";
        int roleId      = (roleIdStr != null && !roleIdStr.trim().isEmpty()) ? Integer.parseInt(roleIdStr.trim()) : 0;

        // Validation
        boolean hasError = false;
        if (fullName.isEmpty()) {
            request.setAttribute("fullNameError", "Full Name is required.");
            hasError = true;
        } else if (fullName.length() > 50) {
            request.setAttribute("fullNameError", "Full Name must not exceed 50 characters.");
            hasError = true;
        } else if (!fullName.matches("[\\p{L} ]+")) {
            request.setAttribute("fullNameError", "Full Name must not contain numbers or special characters.");
            hasError = true;
        }

        if (username.isEmpty()) {
            request.setAttribute("usernameError", "Username is required.");
            hasError = true;
        } else if (username.length() > 50) {
            request.setAttribute("usernameError", "Username must not exceed 50 characters.");
            hasError = true;
        } else if (service.getUserFromUsername(username).isSuccess()) {
            request.setAttribute("usernameError", "This username is already taken. Please choose another one.");
            hasError = true;
        }

        if (email.isEmpty()) {
            request.setAttribute("emailError", "Email is required.");
            hasError = true;
        } else if (email.length() > 50) {
            request.setAttribute("emailError", "Email must not exceed 50 characters.");
            hasError = true;
        } else if (service.getUserFromEmail(email).isSuccess()) {
            request.setAttribute("emailError", "This email is already registered.");
            hasError = true;
        }

        if (roleId == 0) {
            request.setAttribute("roleError", "Role is required.");
            hasError = true;
        }

        if (hasError) {
            request.setAttribute("roles", service.getRoles());
            request.getRequestDispatcher("/admin/newuser.jsp").forward(request, response);
            return;
        }

        // Hệ thống tự tạo password ngẫu nhiên 
        String rawPassword = utils.PasswordUtil.generatePassword();

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(utils.PasswordUtil.hashPassword(rawPassword));
        newUser.setStatus(status);
        newUser.setRoleId(roleId);
        newUser.setCreatedAt(java.time.LocalDate.now());

        UserDTO createResult = service.createUser(newUser);
	if(createResult.isSuccess()) {
		sendAccountMail(Tokens.userAccessToken, email, username, rawPassword);
	}

        request.getSession().setAttribute("flashSuccess", createResult.getMessage());
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr       = request.getParameter("id");
        String rawFullName = request.getParameter("fullName");
        String rawUsername = request.getParameter("username");
        String roleIdStr   = request.getParameter("roleId");
        String status      = request.getParameter("status");

        String fullName = (rawFullName != null) ? rawFullName.trim() : "";
        String username = (rawUsername != null) ? rawUsername.trim() : "";
        int userId      = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;
        int roleId      = (roleIdStr != null && !roleIdStr.isEmpty()) ? Integer.parseInt(roleIdStr) : 0;

        // Validation
        boolean hasError = false;
        if (fullName.isEmpty()) {
            request.setAttribute("fullNameError", "Full Name is required.");
            hasError = true;
        } else if (fullName.length() > 50) {
            request.setAttribute("fullNameError", "Full Name must not exceed 50 characters.");
            hasError = true;
        } else if (!fullName.matches("[\\p{L} ]+")) {
            request.setAttribute("fullNameError", "Full Name must not contain numbers or special characters.");
            hasError = true;
        }

        if (username.isEmpty()) {
            request.setAttribute("usernameError", "Username is required.");
            hasError = true;
        } else if (username.length() > 50) {
            request.setAttribute("usernameError", "Username must not exceed 50 characters.");
            hasError = true;
        } else {
            UserDTO usernameDto = service.getUserFromUsername(username);
            if (usernameDto.isSuccess() && usernameDto.getUser().getId() != userId) {
                request.setAttribute("usernameError", "This username is already taken. Please choose another one.");
                hasError = true;
            }
        }

        if (hasError) {
            UserDTO dto = service.getUserById(userId);
            if (dto.isSuccess()) request.setAttribute("editUser", dto.getUser());
            request.setAttribute("roles", service.getRoles());
            request.getRequestDispatcher("/admin/userdetail.jsp").forward(request, response);
            return;
        }

        // Cập nhật — giữ nguyên email cũ vì không cho sửa
        UserDTO editDto = service.getUserById(userId);
        if (!editDto.isSuccess()) {
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }
        User u = editDto.getUser();
        u.setFullName(fullName);
        u.setUsername(username);
        // email giữ nguyên, không set lại
        u.setRoleId(roleId);
        u.setStatus(status);
        UserDTO updateResult = service.updateUser(u);

        // Nếu role là Expert thì cập nhật danh sách subject
        String[] subjectIds = request.getParameterValues("subjectIds");
        List<Integer> ids = new ArrayList<>();
        if (subjectIds != null) {
            for (String s : subjectIds) ids.add(Integer.parseInt(s));
        }
        // Kiểm tra role mới có phải Expert không
        String roleName = "";
        for (Setting r : service.getRoles()) {
            if (r.getId() == roleId) { roleName = r.getName(); break; }
        }
        if ("Expert".equalsIgnoreCase(roleName)) {
            service.replaceSubjectExpert(userId, ids);
        }

        request.getSession().setAttribute("flashSuccess", updateResult.getMessage());
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

	private void sendAccountMail(String userAccessToken, String recipientEmail, String username, String password) {
		try {
			Gmail service = GmailService.getGmailService(userAccessToken);

			MimeMessage rawMime = GmailService.createEmail(
				recipientEmail,
				"me", // or your actual email address
				"[AMS] Created  account credentials",
				String.format("""
                                                Your account has been created by the Admin!
						Username: %s
						Password: %s                                                 
					""", username, password)
			);

			// "me" tells Google to send as the authenticated OAuth account
			GmailService.sendEmail(service, "me", rawMime);

			System.out.println("Email sent successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

