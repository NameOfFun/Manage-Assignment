package dao;

import dto.SemesterOptionDTO;
import dto.SubmissionDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher/admin side of submissions: the groups of an assignment, grading, Submission List and Details.
 * A submission belongs to a group of the assignment's class.
 *
 * Common parameters: admin = true means "every class", otherwise only the classes of userId.
 * term = null means "every term".
 */
public class SubmissionDAO extends BaseDAO implements AutoCloseable {

    public SubmissionDAO() throws SQLException {
        if (con == null) {
            throw new SQLException("Cannot connect to the database (check BaseDAO settings).");
        }
    }

    public String findRoleName(int roleId) throws SQLException {
        String sql = "SELECT name FROM \"Settings\" WHERE settingid = ? AND type = 'Role'";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("name") : null;
            }
        }
    }

    // ----- Teacher -----

    /**
     * Every group of the assignment's class with its submission (submissionId = 0 when the group
     * has not submitted). Access to the assignment must be checked before calling this.
     */
    public List<SubmissionDTO> findSubmissionsByAssignment(int assignmentId) throws SQLException {
        String sql = "SELECT g.group_id, "
                + "  (SELECT string_agg(u.full_name, ', ' ORDER BY u.full_name) FROM \"GroupStudent\" gs "
                + "     JOIN \"User\" u ON u.user_id = gs.user_id WHERE gs.group_id = g.group_id) AS members, "
                + "  s.submission_id, s.student_id, su.full_name AS student_name, s.submit_time, s.status, "
                + "  s.score, s.feedback, s.evaluated_at, s.file_url, a.due_date "
                + "FROM \"ClassAssignment\" a "
                + "JOIN \"Group\" g ON g.class_id = a.class_id "
                + "LEFT JOIN \"Submission\" s ON s.assignment_id = a.assignment_id AND s.group_id = g.group_id "
                + "LEFT JOIN \"User\" su ON su.user_id = s.student_id "
                + "WHERE a.assignment_id = ? "
                + "ORDER BY g.group_id, s.submit_time DESC NULLS LAST";
        List<SubmissionDTO> submissions = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, assignmentId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    SubmissionDTO submission = new SubmissionDTO();
                    submission.setAssignmentId(assignmentId);
                    submission.setGroupId(result.getInt("group_id"));
                    submission.setGroupMembers(result.getString("members"));
                    submission.setSubmissionId(result.getInt("submission_id"));
                    submission.setStudentId(result.getInt("student_id"));
                    submission.setStudentName(result.getString("student_name"));
                    submission.setSubmitTime(toLocalDateTime(result.getTimestamp("submit_time")));
                    submission.setDueDate(toLocalDateTime(result.getTimestamp("due_date")));
                    submission.setStatus(result.getString("status"));
                    submission.setScore(readScore(result));
                    submission.setFeedback(result.getString("feedback"));
                    submission.setEvaluatedAt(toLocalDateTime(result.getTimestamp("evaluated_at")));
                    submission.setFileUrl(result.getString("file_url"));
                    submissions.add(submission);
                }
            }
        }
        return submissions;
    }

    /** Grades a submission of an assignment in one of the teacher's classes (any class for admins). */
    public boolean gradeSubmission(int submissionId, int assignmentId, double score, String feedback,
            int userId, boolean admin) throws SQLException {
        String sql = "UPDATE \"Submission\" s SET score = ?, feedback = ?, status = ?, teacher_id = ?, evaluated_at = NOW() "
                + "FROM \"ClassAssignment\" a JOIN \"Class\" c ON c.class_id = a.class_id "
                + "WHERE a.assignment_id = s.assignment_id AND s.submission_id = ? AND s.assignment_id = ?";
        if (!admin) {
            sql += " AND c.teacher_id = ?";
        }
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setDouble(1, score);
            statement.setString(2, feedback);
            statement.setString(3, SubmissionDTO.STATUS_GRADED);
            statement.setInt(4, userId);
            statement.setInt(5, submissionId);
            statement.setInt(6, assignmentId);
            if (!admin) {
                statement.setInt(7, userId);
            }
            return statement.executeUpdate() > 0;
        }
    }

    // ----- Submission List / Submission Details -----

    private static final String SUBMISSION_SELECT
            = "SELECT s.submission_id, s.assignment_id, s.student_id, s.group_id, s.submit_time, s.status, "
            + "s.score, s.feedback, s.evaluated_at, s.file_url, "
            + "a.title AS assignment_title, a.status AS assignment_status, a.due_date, "
            + "c.class_id, c.class_code, c.semester, c.year, c.teacher_id AS class_teacher_id, sub.name AS subject_name, "
            + "su.full_name AS student_name, su.username AS student_username, gr.full_name AS grader_name, "
            + "(SELECT string_agg(u.full_name, ', ' ORDER BY u.full_name) FROM \"GroupStudent\" gs "
            + "   JOIN \"User\" u ON u.user_id = gs.user_id WHERE gs.group_id = s.group_id) AS members "
            + "FROM \"Submission\" s "
            + "JOIN \"ClassAssignment\" a ON a.assignment_id = s.assignment_id "
            + "JOIN \"Class\" c ON c.class_id = a.class_id "
            + "LEFT JOIN \"Settings\" sub ON sub.settingid = c.subject_id "
            + "LEFT JOIN \"User\" su ON su.user_id = s.student_id "
            + "LEFT JOIN \"User\" gr ON gr.user_id = s.teacher_id ";

    /**
     * WHERE part of the Submission List: each filter is only applied when the user filled it in.
     * Every time a "?" is added to the SQL, its value is added to params, so they stay in the same order.
     * status: "pending" (not graded), "graded" or "late" (submitted after the due date).
     */
    private static String listWhere(List<Object> params, int userId, boolean admin, SemesterOptionDTO term,
            Integer classId, String status, String keyword) {
        String where = "WHERE 1 = 1 ";
        if (!admin) {
            where += "AND c.teacher_id = ? ";
            params.add(userId);
        }
        if (term != null) {
            where += "AND c.year = ? AND UPPER(c.semester) = UPPER(?) ";
            params.add(term.getYear());
            params.add(term.getSemester());
        }
        if (classId != null) {
            where += "AND c.class_id = ? ";
            params.add(classId);
        }
        if ("pending".equals(status)) {
            where += "AND s.score IS NULL ";
        } else if ("graded".equals(status)) {
            where += "AND s.score IS NOT NULL ";
        } else if ("late".equals(status)) {
            where += "AND s.submit_time > a.due_date ";
        }
        if (keyword != null && !keyword.isBlank()) {
            // Search the assignment title, the student who submitted, and the members of the group
            String search = "%" + keyword.trim() + "%";
            where += "AND (a.title ILIKE ? OR su.full_name ILIKE ? OR su.username ILIKE ? "
                    + "OR EXISTS (SELECT 1 FROM \"GroupStudent\" gs JOIN \"User\" u ON u.user_id = gs.user_id "
                    + "  WHERE gs.group_id = s.group_id AND (u.full_name ILIKE ? OR u.username ILIKE ?))) ";
            for (int i = 0; i < 5; i++) {
                params.add(search);
            }
        }
        return where;
    }

    private static void setParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    public int countSubmissions(int userId, boolean admin, SemesterOptionDTO term, Integer classId,
            String status, String keyword) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM \"Submission\" s "
                + "JOIN \"ClassAssignment\" a ON a.assignment_id = s.assignment_id "
                + "JOIN \"Class\" c ON c.class_id = a.class_id "
                + "LEFT JOIN \"User\" su ON su.user_id = s.student_id "
                + listWhere(params, userId, admin, term, classId, status, keyword);
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    /** One page of the Submission List: ungraded first, then newest submissions first. */
    public List<SubmissionDTO> findSubmissions(int userId, boolean admin, SemesterOptionDTO term, Integer classId,
            String status, String keyword, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = SUBMISSION_SELECT
                + listWhere(params, userId, admin, term, classId, status, keyword)
                + "ORDER BY (s.score IS NULL) DESC, s.submit_time DESC NULLS LAST, s.submission_id DESC "
                + "LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<SubmissionDTO> submissions = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    submissions.add(readSubmission(result));
                }
            }
        }
        return submissions;
    }

    /** A submission of one of the teacher's classes (any class for admins), or null. */
    public SubmissionDTO findSubmissionDetail(int submissionId, int userId, boolean admin) throws SQLException {
        String sql = SUBMISSION_SELECT + "WHERE s.submission_id = ?";
        if (!admin) {
            sql += " AND c.teacher_id = ?";
        }
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, submissionId);
            if (!admin) {
                statement.setInt(2, userId);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readSubmission(result) : null;
            }
        }
    }

    private static SubmissionDTO readSubmission(ResultSet result) throws SQLException {
        SubmissionDTO submission = new SubmissionDTO();
        submission.setSubmissionId(result.getInt("submission_id"));
        submission.setAssignmentId(result.getInt("assignment_id"));
        submission.setStudentId(result.getInt("student_id"));
        submission.setStudentName(result.getString("student_name"));
        submission.setStudentUsername(result.getString("student_username"));
        submission.setGroupId(result.getInt("group_id"));
        submission.setGroupMembers(result.getString("members"));
        submission.setSubmitTime(toLocalDateTime(result.getTimestamp("submit_time")));
        submission.setDueDate(toLocalDateTime(result.getTimestamp("due_date")));
        submission.setStatus(result.getString("status"));
        submission.setScore(readScore(result));
        submission.setFeedback(result.getString("feedback"));
        submission.setEvaluatedAt(toLocalDateTime(result.getTimestamp("evaluated_at")));
        submission.setFileUrl(result.getString("file_url"));
        submission.setGraderName(result.getString("grader_name"));
        submission.setAssignmentTitle(result.getString("assignment_title"));
        submission.setAssignmentStatus(result.getString("assignment_status"));
        submission.setClassId(result.getInt("class_id"));
        submission.setClassCode(result.getString("class_code"));
        submission.setSemester(result.getString("semester"));
        submission.setYear(result.getInt("year"));
        submission.setClassTeacherId(result.getInt("class_teacher_id"));
        submission.setSubjectName(result.getString("subject_name"));
        return submission;
    }

    // ----- File download -----

    /** file_url of a submission in one of the teacher's classes (any class for admins), or null. */
    public String findAccessibleFileUrl(int submissionId, int userId, boolean admin) throws SQLException {
        String sql = "SELECT s.file_url FROM \"Submission\" s "
                + "JOIN \"ClassAssignment\" a ON a.assignment_id = s.assignment_id "
                + "JOIN \"Class\" c ON c.class_id = a.class_id "
                + "WHERE s.submission_id = ?";
        if (!admin) {
            sql += " AND c.teacher_id = ?";
        }
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, submissionId);
            if (!admin) {
                statement.setInt(2, userId);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("file_url") : null;
            }
        }
    }

    private static Double readScore(ResultSet result) throws SQLException {
        double score = result.getDouble("score");
        return result.wasNull() ? null : score;
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
