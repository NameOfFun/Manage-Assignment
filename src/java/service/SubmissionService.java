package service;

import dao.SubmissionDAO;
import dto.PageDTO;
import dto.SemesterOptionDTO;
import dto.SubmissionDTO;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/** Teacher/admin side of submissions: listing, details, grading and opening the submitted work. */
public class SubmissionService {

    private static final int MAX_FEEDBACK_LENGTH = 2000;

    private final Path uploadDir;

    /** @param uploadDir folder where uploaded submission files ("upload:" file_url values) are stored. */
    public SubmissionService(Path uploadDir) {
        this.uploadDir = uploadDir.toAbsolutePath().normalize();
    }

    /**
     * Upload folder from the "submissionUploadDir" context-param in web.xml, or ~/ams-uploads/submissions.
     * It is outside the web app so files survive a redeploy.
     */
    public static SubmissionService fromContext(ServletContext context) {
        String configured = context.getInitParameter("submissionUploadDir");
        Path dir = configured == null || configured.isBlank()
                ? Path.of(System.getProperty("user.home"), "ams-uploads", "submissions")
                : Path.of(configured.trim());
        return new SubmissionService(dir);
    }

    public boolean isAdmin(int roleId) throws SQLException {
        try (SubmissionDAO dao = new SubmissionDAO()) {
            return "Admin".equalsIgnoreCase(dao.findRoleName(roleId));
        }
    }

    /** Groups of the class with their submissions. Check access to the assignment first. */
    public List<SubmissionDTO> getSubmissions(int assignmentId) throws SQLException {
        try (SubmissionDAO dao = new SubmissionDAO()) {
            return dao.findSubmissionsByAssignment(assignmentId);
        }
    }

    /** One page of the Submission List. A page number that is too large shows the last page. */
    public PageDTO<SubmissionDTO> getSubmissionPage(int userId, boolean admin, SemesterOptionDTO term, Integer classId,
            String status, String keyword, int page, int pageSize) throws SQLException {
        try (SubmissionDAO dao = new SubmissionDAO()) {
            int total = dao.countSubmissions(userId, admin, term, classId, status, keyword);
            int current = PageDTO.clampPage(page, total, pageSize);
            int offset = (current - 1) * pageSize;
            List<SubmissionDTO> items = dao.findSubmissions(userId, admin, term, classId, status, keyword,
                    offset, pageSize);
            return new PageDTO<>(items, current, pageSize, total);
        }
    }

    /** The submission if it belongs to one of the teacher's classes (any class for admins), otherwise null. */
    public SubmissionDTO getSubmissionDetail(int submissionId, int userId, boolean admin) throws SQLException {
        try (SubmissionDAO dao = new SubmissionDAO()) {
            return dao.findSubmissionDetail(submissionId, userId, admin);
        }
    }

    public boolean grade(int submissionId, int assignmentId, String scoreText, String feedback,
            int userId, boolean admin) throws SQLException {
        double score = parseScore(scoreText);
        // Keep at most 2 decimals, e.g. 7.256 becomes 7.26
        double roundedScore = Math.round(score * 100) / 100.0;

        String cleanFeedback = null;
        if (feedback != null && !feedback.isBlank()) {
            cleanFeedback = feedback.trim();
        }
        if (cleanFeedback != null && cleanFeedback.length() > MAX_FEEDBACK_LENGTH) {
            throw new IllegalArgumentException("Feedback can be at most " + MAX_FEEDBACK_LENGTH + " characters.");
        }

        try (SubmissionDAO dao = new SubmissionDAO()) {
            return dao.gradeSubmission(submissionId, assignmentId, roundedScore, cleanFeedback, userId, admin);
        }
    }

    // A number from 0 to 10; both 7.5 and 7,5 are accepted.
    private double parseScore(String scoreText) {
        String errorMessage = "Score must be a number from 0 to 10.";
        if (scoreText == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        double score;
        try {
            score = Double.parseDouble(scoreText.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
        if (Double.isNaN(score) || score < 0 || score > 10) {
            throw new IllegalArgumentException(errorMessage);
        }
        return score;
    }

    /** file_url of a submission the user may open (class teacher or admin), or null. */
    public String getAccessibleFileUrl(int submissionId, int userId, boolean admin) throws SQLException {
        try (SubmissionDAO dao = new SubmissionDAO()) {
            return dao.findAccessibleFileUrl(submissionId, userId, admin);
        }
    }

    /** Saves an uploaded file in the upload folder. */
    public void saveFile(InputStream content, String storedName) throws IOException {
        Files.createDirectories(uploadDir);
        Files.copy(content, uploadDir.resolve(storedName));
    }

    public void deleteFile(String storedName) throws IOException {
        Path file = resolveStoredFile(storedName);
        if (file != null) {
            Files.deleteIfExists(file);
        }
    }

    /** Path of an uploaded file inside the upload folder, or null if the name escapes it. */
    public Path resolveStoredFile(String storedName) {
        Path path = uploadDir.resolve(storedName).normalize();
        return path.startsWith(uploadDir) ? path : null;
    }
}
