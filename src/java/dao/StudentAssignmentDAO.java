package dao;

import dto.AssignmentDTO;
import dto.ClassDTO;
import dto.StudentAssignmentDTO;
import dto.SubmissionDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Student side of assignments: seeing the assignments of their classes and submitting for their group. */
public class StudentAssignmentDAO extends BaseDAO implements AutoCloseable {

    // Students only see assignments that have started and are not drafts
    private static final String VISIBLE = "a.start_date <= NOW() AND COALESCE(UPPER(a.status), '') <> 'DRAFT' ";

    // Each assignment of the student's classes with the student's group and that group's submission.
    // Takes 2 parameters: studentId, studentId.
    private static final String SELECT_ROW = "SELECT a.assignment_id, a.title, a.instruction, a.start_date, a.due_date, a.status, "
            + "c.class_id, c.class_code, c.semester, c.year, sub.name AS subject_name, my_group.group_id, "
            + "s.submission_id, s.student_id, su.full_name AS student_name, s.submit_time, "
            + "s.status AS submission_status, s.score, s.feedback, s.evaluated_at, s.file_url "
            + "FROM \"ClassAssignment\" a "
            + "JOIN \"Class\" c ON c.class_id = a.class_id "
            + "JOIN \"ClassStudent\" cs ON cs.class_id = a.class_id AND cs.student_id = ? "
            + "LEFT JOIN \"Settings\" sub ON sub.settingid = c.subject_id "
            + "LEFT JOIN (SELECT g.class_id, MIN(g.group_id) AS group_id FROM \"Group\" g "
            + "           JOIN \"GroupStudent\" gs ON gs.group_id = g.group_id "
            + "           WHERE gs.user_id = ? GROUP BY g.class_id) my_group ON my_group.class_id = c.class_id "
            + "LEFT JOIN \"Submission\" s ON s.assignment_id = a.assignment_id AND s.group_id = my_group.group_id "
            + "LEFT JOIN \"User\" su ON su.user_id = s.student_id ";

    public StudentAssignmentDAO() throws SQLException {
        if (con == null) {
            throw new SQLException("Cannot connect to the database (check BaseDAO settings).");
        }
    }

    public String findRoleName(int roleId) throws SQLException {
        String sql = "SELECT name FROM \"Settings\" WHERE settingid = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("name") : null;
            }
        }
    }

    /** The class if the student is enrolled in it, otherwise null. */
    public ClassDTO findClass(int studentId, int classId) throws SQLException {
        String sql = "SELECT c.class_id, c.class_code, c.semester, c.year, sub.name AS subject_name, t.full_name AS teacher_name "
                + "FROM \"Class\" c "
                + "JOIN \"ClassStudent\" cs ON cs.class_id = c.class_id AND cs.student_id = ? "
                + "LEFT JOIN \"Settings\" sub ON sub.settingid = c.subject_id "
                + "LEFT JOIN \"User\" t ON t.user_id = c.teacher_id "
                + "WHERE c.class_id = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, classId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new ClassDTO(
                        result.getInt("class_id"),
                        result.getString("class_code"),
                        result.getString("subject_name"),
                        result.getString("semester"),
                        result.getInt("year"),
                        result.getString("teacher_name"),
                        0);
            }
        }
    }

    public int countAssignments(int studentId, int classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"ClassAssignment\" a "
                + "JOIN \"ClassStudent\" cs ON cs.class_id = a.class_id AND cs.student_id = ? "
                + "WHERE " + VISIBLE + "AND a.class_id = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, classId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    /** One page of the assignments of a class, latest deadline first. */
    public List<StudentAssignmentDTO> findAssignments(int studentId, int classId, int offset, int limit) throws SQLException {
        String sql = SELECT_ROW + "WHERE " + VISIBLE + "AND a.class_id = ? "
                + "ORDER BY a.due_date DESC, a.assignment_id DESC LIMIT ? OFFSET ?";
        List<StudentAssignmentDTO> rows = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, studentId);
            statement.setInt(3, classId);
            statement.setInt(4, limit);
            statement.setInt(5, offset);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(readRow(result));
                }
            }
        }
        return rows;
    }

    /** The assignment if the student can see it, otherwise null. */
    public StudentAssignmentDTO findAssignment(int studentId, int assignmentId) throws SQLException {
        String sql = SELECT_ROW + "WHERE " + VISIBLE + "AND a.assignment_id = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, studentId);
            statement.setInt(3, assignmentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readRow(result) : null;
            }
        }
    }

    public void createSubmission(int assignmentId, int studentId, int groupId, String fileUrl) throws SQLException {
        String sql = "INSERT INTO \"Submission\" (assignment_id, student_id, group_id, submit_time, status, file_url) "
                + "VALUES (?, ?, ?, NOW(), ?, ?)";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, assignmentId);
            statement.setInt(2, studentId);
            statement.setInt(3, groupId);
            statement.setString(4, SubmissionDTO.STATUS_SUBMITTED);
            statement.setString(5, fileUrl);
            statement.executeUpdate();
        }
    }

    /** Replaces the work of a submission. Returns false if the teacher has graded it in the meantime. */
    public boolean updateSubmission(int submissionId, int studentId, String fileUrl) throws SQLException {
        String sql = "UPDATE \"Submission\" SET student_id = ?, submit_time = NOW(), file_url = ? "
                + "WHERE submission_id = ? AND score IS NULL";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setString(2, fileUrl);
            statement.setInt(3, submissionId);
            return statement.executeUpdate() > 0;
        }
    }

    private static StudentAssignmentDTO readRow(ResultSet result) throws SQLException {
        AssignmentDTO assignment = new AssignmentDTO();
        assignment.setAssignmentId(result.getInt("assignment_id"));
        assignment.setTitle(result.getString("title"));
        assignment.setInstruction(result.getString("instruction"));
        assignment.setStartDate(toLocalDateTime(result.getTimestamp("start_date")));
        assignment.setDueDate(toLocalDateTime(result.getTimestamp("due_date")));
        assignment.setStatus(result.getString("status"));
        assignment.setClassId(result.getInt("class_id"));
        assignment.setClassCode(result.getString("class_code"));
        assignment.setSemester(result.getString("semester"));
        assignment.setYear(result.getInt("year"));
        assignment.setSubjectName(result.getString("subject_name"));

        StudentAssignmentDTO row = new StudentAssignmentDTO();
        row.setAssignment(assignment);
        row.setGroupId(result.getInt("group_id"));

        int submissionId = result.getInt("submission_id");
        if (submissionId > 0) {
            SubmissionDTO submission = new SubmissionDTO();
            submission.setSubmissionId(submissionId);
            submission.setAssignmentId(assignment.getAssignmentId());
            submission.setGroupId(row.getGroupId());
            submission.setStudentId(result.getInt("student_id"));
            submission.setStudentName(result.getString("student_name"));
            submission.setSubmitTime(toLocalDateTime(result.getTimestamp("submit_time")));
            submission.setDueDate(assignment.getDueDate());
            submission.setStatus(result.getString("submission_status"));
            double score = result.getDouble("score");
            if (!result.wasNull()) {
                submission.setScore(score);
            }
            submission.setFeedback(result.getString("feedback"));
            submission.setEvaluatedAt(toLocalDateTime(result.getTimestamp("evaluated_at")));
            submission.setFileUrl(result.getString("file_url"));
            row.setSubmission(submission);
        }
        return row;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @Override
    public void close() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }
}
