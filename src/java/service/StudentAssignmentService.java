package service;

import dao.StudentAssignmentDAO;
import dto.ClassDTO;
import dto.PageDTO;
import dto.StudentAssignmentDTO;
import dto.SubmissionDTO;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Rules for a student seeing assignments and submitting for their group (one submission per group). */
public class StudentAssignmentService {

    public static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB

    private static final Logger LOG = Logger.getLogger(StudentAssignmentService.class.getName());
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "zip", "rar", "7z", "png", "jpg", "jpeg");
    private static final int MAX_LINK_LENGTH = 255;
    private static final int MAX_FILE_NAME_LENGTH = 180; // file_url is varchar(255) and also holds the prefix

    private final SubmissionService fileStore;

    /** @param fileStore knows the folder where uploaded submission files are kept */
    public StudentAssignmentService(SubmissionService fileStore) {
        this.fileStore = fileStore;
    }

    public boolean isStudent(int roleId) throws SQLException {
        try (StudentAssignmentDAO dao = new StudentAssignmentDAO()) {
            return "Student".equalsIgnoreCase(dao.findRoleName(roleId));
        }
    }

    /** The class if the student is enrolled in it, otherwise null. */
    public ClassDTO getClass(int studentId, int classId) throws SQLException {
        try (StudentAssignmentDAO dao = new StudentAssignmentDAO()) {
            return dao.findClass(studentId, classId);
        }
    }

    /** One page of the assignments of a class. A page number that is too large shows the last page. */
    public PageDTO<StudentAssignmentDTO> getAssignmentPage(int studentId, int classId, int page, int pageSize) throws SQLException {
        try (StudentAssignmentDAO dao = new StudentAssignmentDAO()) {
            int total = dao.countAssignments(studentId, classId);
            int current = PageDTO.clampPage(page, total, pageSize);
            int offset = (current - 1) * pageSize;
            List<StudentAssignmentDTO> items = dao.findAssignments(studentId, classId, offset, pageSize);
            return new PageDTO<>(items, current, pageSize, total);
        }
    }

    /** The assignment if the student can see it, otherwise null. */
    public StudentAssignmentDTO getAssignment(int studentId, int assignmentId) throws SQLException {
        try (StudentAssignmentDAO dao = new StudentAssignmentDAO()) {
            return dao.findAssignment(studentId, assignmentId);
        }
    }

    /**
     * Saves the group's work: either a file or a link. Submitting again replaces the previous work.
     *
     * @return false if the student cannot see this assignment
     * @throws IllegalArgumentException with a message for the user if the work cannot be accepted
     */
    public boolean submit(int studentId, int assignmentId, Part file, String link) throws SQLException, IOException {
        try (StudentAssignmentDAO dao = new StudentAssignmentDAO()) {
            StudentAssignmentDTO row = dao.findAssignment(studentId, assignmentId);
            if (row == null) {
                return false;
            }
            if (!row.isCanSubmit()) {
                throw new IllegalArgumentException(row.getBlockedReason());
            }

            boolean hasFile = file != null && file.getSize() > 0;
            String cleanLink = link == null ? "" : link.trim();
            boolean hasLink = !cleanLink.isEmpty();
            if (hasFile == hasLink) {
                throw new IllegalArgumentException("Please attach a file or enter a link (only one of them).");
            }

            String fileUrl;
            String newStoredName = null;
            if (hasFile) {
                newStoredName = storeFile(file, assignmentId, row.getGroupId());
                fileUrl = SubmissionDTO.UPLOAD_PREFIX + newStoredName;
            } else {
                checkLink(cleanLink);
                fileUrl = cleanLink;
            }

            SubmissionDTO previous = row.getSubmission();
            try {
                if (previous == null) {
                    dao.createSubmission(assignmentId, studentId, row.getGroupId(), fileUrl);
                } else if (!dao.updateSubmission(previous.getSubmissionId(), studentId, fileUrl)) {
                    throw new IllegalArgumentException("This submission has already been graded and cannot be changed.");
                }
            } catch (SQLException | IllegalArgumentException e) {
                deleteFileQuietly(newStoredName); // nothing refers to the new file
                throw e;
            }

            if (previous != null && previous.isUploaded()) {
                deleteFileQuietly(previous.getStoredFileName());
            }
            return true;
        }
    }

    // Checks the file and saves it in the upload folder. Returns the name it was stored under.
    private String storeFile(Part file, int assignmentId, int groupId) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("The file cannot be larger than 20 MB.");
        }
        String fileName = cleanFileName(file.getSubmittedFileName());
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("This file type is not allowed. Allowed types: "
                    + String.join(", ", ALLOWED_EXTENSIONS) + ".");
        }

        // SubmissionDTO.getFileName() removes this "a1_g2_123456_" prefix to show the original name
        String storedName = "a" + assignmentId + "_g" + groupId + "_" + System.currentTimeMillis() + "_" + fileName;
        try (InputStream content = file.getInputStream()) {
            fileStore.saveFile(content, storedName);
        }
        return storedName;
    }

    // Keeps only the file name itself and replaces the characters that are not allowed in file names.
    private String cleanFileName(String submittedName) {
        String name = submittedName == null ? "" : submittedName;
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        name = name.substring(lastSlash + 1);
        name = name.replaceAll("[:*?\"<>|]", "_").trim();
        if (name.length() > MAX_FILE_NAME_LENGTH) {
            name = name.substring(name.length() - MAX_FILE_NAME_LENGTH); // the end has the extension
        }
        return name;
    }

    private void checkLink(String link) {
        boolean web = link.startsWith("http://") || link.startsWith("https://");
        if (!web || link.contains(" ")) {
            throw new IllegalArgumentException("Please enter a valid link starting with http:// or https://.");
        }
        if (link.length() > MAX_LINK_LENGTH) {
            throw new IllegalArgumentException("The link can be at most " + MAX_LINK_LENGTH + " characters.");
        }
    }

    private void deleteFileQuietly(String storedName) {
        if (storedName == null) {
            return;
        }
        try {
            fileStore.deleteFile(storedName);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot delete unused submission file " + storedName, e);
        }
    }
}
