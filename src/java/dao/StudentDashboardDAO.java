package dao;

import dto.RecentGradeDTO;
import dto.ClassDTO;
import dto.AssignmentDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.User;


public class StudentDashboardDAO extends BaseDAO implements AutoCloseable {

    /**
     * A submission belongs to the student if they submitted it themselves or it was submitted
     * for a group they are in. Takes 2 parameters: studentId, studentId.
     */
    private static final String SUBMISSION_OF_STUDENT
            = "(s.student_id = ? OR s.group_id IN "
            + "(SELECT gs.group_id FROM \"GroupStudent\" gs WHERE gs.user_id = ?))";

    /** Assignments students can see: already started and not a draft. */
    private static final String ASSIGNMENT_VISIBLE
            = "a.start_date <= NOW() AND COALESCE(UPPER(a.status), '') <> 'DRAFT'";

    public StudentDashboardDAO() throws SQLException {
        super();
        if (con == null) {
            throw new SQLException("Cannot connect to the database (check BaseDAO settings).");
        }
    }

    /** Returns the user with this username, or null. */
    public User findUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, role_id, username, email, full_name, status "
                + "FROM \"User\" WHERE username = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet r = st.executeQuery()) {
                if (!r.next()) {
                    return null;
                }
                User u = new User();
                u.setId(r.getInt("user_id"));
                u.setRoleId(r.getInt("role_id"));
                u.setUsername(r.getString("username"));
                u.setEmail(r.getString("email"));
                u.setFullName(r.getString("full_name"));
                u.setStatus(r.getString("status"));
                return u;
            }
        }
    }

    /** Role name stored in Settings (e.g. "Student"), or null if the role id is unknown. */
    public String findRoleName(int roleId) throws SQLException {
        String sql = "SELECT name FROM \"Settings\" WHERE settingid = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, roleId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getString("name") : null;
            }
        }
    }

    /** Classes the student is enrolled in, newest term first. */
    public List<ClassDTO> findClasses(int studentId) throws SQLException {
        String sql = "SELECT c.class_id, c.class_code, c.semester, c.year, "
                + "       sub.name AS subject_name, t.full_name AS teacher_name, "
                + "       (SELECT COUNT(*) FROM \"ClassAssignment\" a "
                + "         WHERE a.class_id = c.class_id AND " + ASSIGNMENT_VISIBLE + ") AS assignment_count "
                + "FROM \"ClassStudent\" cs "
                + "JOIN \"Class\" c ON c.class_id = cs.class_id "
                + "LEFT JOIN \"Settings\" sub ON sub.settingid = c.subject_id "
                + "LEFT JOIN \"User\" t ON t.user_id = c.teacher_id "
                + "WHERE cs.student_id = ? "
                + "ORDER BY c.year DESC, "
                + "         CASE UPPER(c.semester) WHEN 'FALL' THEN 3 WHEN 'SUMMER' THEN 2 WHEN 'SPRING' THEN 1 ELSE 0 END DESC, "
                + "         c.class_code";
        List<ClassDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, studentId);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(new ClassDTO(
                            r.getInt("class_id"),
                            r.getString("class_code"),
                            r.getString("subject_name"),
                            r.getString("semester"),
                            r.getInt("year"),
                            r.getString("teacher_name"),
                            r.getInt("assignment_count")));
                }
            }
        }
        return list;
    }

    /** Assignments in the student's classes whose deadline has not passed, soonest first. */
    public List<AssignmentDTO> findUpcomingAssignments(int studentId, int limit) throws SQLException {
        String sql = "SELECT a.assignment_id, a.title, a.due_date, c.class_code, "
                + "       EXISTS (SELECT 1 FROM \"Submission\" s "
                + "                WHERE s.assignment_id = a.assignment_id AND " + SUBMISSION_OF_STUDENT + ") AS submitted "
                + "FROM \"ClassAssignment\" a "
                + "JOIN \"Class\" c ON c.class_id = a.class_id "
                + "JOIN \"ClassStudent\" cs ON cs.class_id = a.class_id AND cs.student_id = ? "
                + "WHERE a.due_date >= NOW() AND " + ASSIGNMENT_VISIBLE + " "
                + "ORDER BY a.due_date "
                + "LIMIT ?";
        List<AssignmentDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, studentId);
            st.setInt(2, studentId);
            st.setInt(3, studentId);
            st.setInt(4, limit);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(new AssignmentDTO(
                            r.getInt("assignment_id"),
                            r.getString("title"),
                            r.getString("class_code"),
                            toLocalDateTime(r.getTimestamp("due_date")),
                            r.getBoolean("submitted")));
                }
            }
        }
        return list;
    }

    /** Number of open assignments (deadline not passed) the student has not submitted yet. */
    public int countPendingAssignments(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM \"ClassAssignment\" a "
                + "JOIN \"ClassStudent\" cs ON cs.class_id = a.class_id AND cs.student_id = ? "
                + "WHERE a.due_date >= NOW() AND " + ASSIGNMENT_VISIBLE + " "
                + "  AND NOT EXISTS (SELECT 1 FROM \"Submission\" s "
                + "                   WHERE s.assignment_id = a.assignment_id AND " + SUBMISSION_OF_STUDENT + ")";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, studentId);
            st.setInt(2, studentId);
            st.setInt(3, studentId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    /** Number of distinct assignments the student (or their group) has submitted. */
    public int countSubmittedAssignments(int studentId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT s.assignment_id) FROM \"Submission\" s "
                + "WHERE " + SUBMISSION_OF_STUDENT;
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, studentId);
            st.setInt(2, studentId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    /** Latest graded submissions of the student, newest first. */
    public List<RecentGradeDTO> findRecentGrades(int studentId, int limit) throws SQLException {
        String sql = "SELECT a.title, c.class_code, s.score, s.feedback, s.evaluated_at "
                + "FROM \"Submission\" s "
                + "JOIN \"ClassAssignment\" a ON a.assignment_id = s.assignment_id "
                + "JOIN \"Class\" c ON c.class_id = a.class_id "
                + "WHERE " + SUBMISSION_OF_STUDENT + " AND s.score IS NOT NULL "
                + "ORDER BY s.evaluated_at DESC NULLS LAST, s.submission_id DESC "
                + "LIMIT ?";
        List<RecentGradeDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, studentId);
            st.setInt(2, studentId);
            st.setInt(3, limit);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(new RecentGradeDTO(
                            r.getString("title"),
                            r.getString("class_code"),
                            r.getDouble("score"),
                            r.getString("feedback"),
                            toLocalDateTime(r.getTimestamp("evaluated_at"))));
                }
            }
        }
        return list;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    @Override
    public void close() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            // Nothing useful to do if closing fails; the connection is discarded either way.
        }
    }
}
