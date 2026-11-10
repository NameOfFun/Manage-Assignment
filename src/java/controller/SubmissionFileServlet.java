package controller;

import dto.SubmissionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;
import service.SubmissionService;
import utils.SessionUtil;

/**
 * /submission/file?id=7 → downloads an uploaded submission file, or redirects to a submitted link.
 * Allowed for the class teacher and admins.
 */
public class SubmissionFileServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SubmissionFileServlet.class.getName());
    private SubmissionService service;

    @Override
    public void init() {
        service = SubmissionService.fromContext(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = SessionUtil.validateSession(request, response);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid submission ID.");
            return;
        }

        SubmissionDTO submission = new SubmissionDTO();
        try {
            submission.setFileUrl(service.getAccessibleFileUrl(id, user.getId(), service.isAdmin(user.getRoleId())));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Cannot load submission " + id, e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Cannot connect to the database.");
            return;
        }
        if (submission.getFileUrl() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The submission does not exist or you do not have permission to view it.");
            return;
        }
        if (!submission.isUploaded()) {
            response.sendRedirect(submission.getFileUrl());
            return;
        }

        Path file = service.resolveStoredFile(submission.getStoredFileName());
        if (file == null || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The submitted file is no longer on the server.");
            return;
        }
        String contentType = getServletContext().getMimeType(file.getFileName().toString());
        response.setContentType(contentType == null ? "application/octet-stream" : contentType);
        response.setContentLengthLong(Files.size(file));
        String encodedName = URLEncoder.encode(submission.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
        response.setHeader("X-Content-Type-Options", "nosniff");
        Files.copy(file, response.getOutputStream());
    }
}
