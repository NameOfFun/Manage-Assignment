package service;

import dao.AssignmentDAO;
import dto.AssignmentDTO;
import dto.AssignmentTemplateDTO;
import dto.PageDTO;
import dto.SemesterOptionDTO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import model.Assignment;
import model.Class;
import model.Setting;

/**
 * Rules of the teacher dashboard and assignment screens.
 * admin = true means "every class", otherwise only the classes of userId. term = null means "every term".
 */
public class AssignmentService {

    private static final String DUPLICATE_TITLE = "This class already has an assignment with the same name. Please choose another name.";

    public String getManagerRoleName(int roleId) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            String roleName = dao.findRoleName(roleId);
            if ("Admin".equalsIgnoreCase(roleName) || "Teacher".equalsIgnoreCase(roleName)) {
                return roleName;
            }
            return null;
        }
    }

    /** The newest term the teacher has a class in, or null if the teacher has no class. */
    public SemesterOptionDTO getCurrentTerm(int teacherId) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findCurrentTerm(teacherId);
        }
    }

    public List<Class> getClasses(int userId, boolean admin, SemesterOptionDTO term) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findClasses(userId, admin, term);
        }
    }

    public List<Setting> getSubjects(int userId, boolean admin, SemesterOptionDTO term) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findSubjects(userId, admin, term);
        }
    }

    public List<AssignmentTemplateDTO> getTemplates(int userId, boolean admin) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findTemplates(userId, admin);
        }
    }

    /** One page of the Assignment List. A page number that is too large shows the last page. */
    public PageDTO<AssignmentDTO> getAssignmentPage(int userId, boolean admin, SemesterOptionDTO term, Integer subjectId,
            Integer classId, String status, String keyword, int page, int pageSize) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            int total = dao.countAssignments(userId, admin, term, subjectId, classId, status, keyword);
            int current = PageDTO.clampPage(page, total, pageSize);
            int offset = (current - 1) * pageSize;
            List<AssignmentDTO> items = dao.findAssignments(userId, admin, term, subjectId, classId, status, keyword,
                    offset, pageSize);
            return new PageDTO<>(items, current, pageSize, total);
        }
    }

    public int countTeacherAssignments(int teacherId, SemesterOptionDTO term) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.countAssignments(teacherId, false, term, null, null, null, null);
        }
    }

    public List<AssignmentDTO> getRecentAssignments(int teacherId, int limit) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findRecentAssignments(teacherId, limit);
        }
    }

    public List<AssignmentDTO> getAssignmentsToEvaluate(int teacherId) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findAssignmentsToEvaluate(teacherId);
        }
    }

    public AssignmentDTO getAssignmentById(int assignmentId, int userId, boolean admin) throws SQLException {
        try (AssignmentDAO dao = new AssignmentDAO()) {
            return dao.findAssignmentById(assignmentId, userId, admin);
        }
    }

    /** Validates and saves a new assignment. Returns its id, or 0 if the user cannot use this class or template. */
    public int addAssignment(Assignment assignment, int userId, boolean admin) throws SQLException {
        if (assignment.getClassId() <= 0) {
            throw new IllegalArgumentException("Please choose a class.");
        }
        validateContent(assignment);
        checkStartDateNotPast(assignment);
        checkDueDateInFuture(assignment);
        String status = assignment.getStatus();
        if (!Assignment.STATUS_DRAFT.equals(status) && !Assignment.STATUS_PUBLISHED.equals(status)) {
            throw new IllegalArgumentException("Invalid status.");
        }
        if (assignment.getTemplateId() != null && assignment.getTemplateId() <= 0) {
            throw new IllegalArgumentException("Invalid template.");
        }

        try (AssignmentDAO dao = new AssignmentDAO()) {
            if (dao.titleExists(assignment.getClassId(), assignment.getTitle(), 0)) {
                throw new IllegalArgumentException(DUPLICATE_TITLE);
            }
            if (!canUseClassAndTemplate(dao, assignment, userId, admin)) {
                return 0;
            }
            return dao.createAssignment(assignment);
        }
    }

    /** Validates and saves the changes. Returns false if the assignment does not exist or the user cannot edit it. */
    public boolean updateAssignment(Assignment assignment, int userId, boolean admin) throws SQLException {
        if (assignment.getAssignmentId() <= 0) {
            throw new IllegalArgumentException("Invalid assignment.");
        }
        validateContent(assignment);
        String status = assignment.getStatus();
        if (!Assignment.STATUS_DRAFT.equals(status) && !Assignment.STATUS_PUBLISHED.equals(status)
                && !Assignment.STATUS_CLOSED.equals(status)) {
            throw new IllegalArgumentException("Invalid status.");
        }

        try (AssignmentDAO dao = new AssignmentDAO()) {
            AssignmentDTO current = dao.findAssignmentById(assignment.getAssignmentId(), userId, admin);
            if (current == null) {
                return false;
            }
            // Only changed dates are checked, so an assignment that already started can still have its text fixed
            if (!sameMinute(assignment.getStartDate(), current.getStartDate())) {
                checkStartDateNotPast(assignment);
            }
            if (!sameMinute(assignment.getDueDate(), current.getDueDate())) {
                checkDueDateInFuture(assignment);
            }

            // Only a renamed title is checked, so old duplicates can still be edited
            boolean renamed = !assignment.getTitle().equalsIgnoreCase(current.getTitle().trim());
            if (renamed && dao.titleExists(current.getClassId(), assignment.getTitle(), assignment.getAssignmentId())) {
                throw new IllegalArgumentException(DUPLICATE_TITLE);
            }
            return dao.updateAssignment(assignment, userId, admin);
        }
    }

    private boolean canUseClassAndTemplate(AssignmentDAO dao, Assignment assignment, int userId, boolean admin)
            throws SQLException {
        if (!dao.canManageClass(assignment.getClassId(), userId, admin)) {
            return false;
        }
        if (assignment.getTemplateId() == null) {
            return true;
        }
        return dao.templateMatchesClass(assignment.getTemplateId(), assignment.getClassId());
    }

    private void checkStartDateNotPast(Assignment assignment) {
        if (assignment.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }
    }

    private void checkDueDateInFuture(Assignment assignment) {
        if (!assignment.getDueDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future.");
        }
    }

    // The form sends minutes only, the database may store seconds.
    private static boolean sameMinute(LocalDateTime first, LocalDateTime second) {
        return first.truncatedTo(ChronoUnit.MINUTES).equals(second.truncatedTo(ChronoUnit.MINUTES));
    }

    // Checks the name, instruction and date order, and removes the outer spaces of the text fields.
    private void validateContent(Assignment assignment) {
        String title = assignment.getTitle() == null ? "" : assignment.getTitle().trim();
        if (title.isEmpty() || title.length() > 255) {
            throw new IllegalArgumentException("Assignment name is required and must be at most 255 characters.");
        }
        String instruction = assignment.getInstruction() == null ? "" : assignment.getInstruction().trim();
        if (instruction.isEmpty()) {
            throw new IllegalArgumentException("Instruction is required.");
        }
        if (!assignment.getDueDate().isAfter(assignment.getStartDate())) {
            throw new IllegalArgumentException("Due date must be after the start date.");
        }
        assignment.setTitle(title);
        assignment.setInstruction(instruction);
    }
}
