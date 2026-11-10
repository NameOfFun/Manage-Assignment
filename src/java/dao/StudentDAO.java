package dao;

import dto.StudentClassDTO;
import dto.StudentRowDTO;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.User;
import utils.SearchUtil;

public class StudentDAO extends BaseDAO implements AutoCloseable {

    public StudentDAO() throws SQLException {
        super();
        if (con == null) {
            throw new SQLException("Cannot connect to the database (check BaseDAO settings).");
        }
    }

    private static final String FROM
            = "FROM \"ClassStudent\" cs "
            + "JOIN \"Class\" c ON c.class_id = cs.class_id "
            + "JOIN \"User\" u ON u.user_id = cs.student_id ";

    private static String where(int teacherId, Integer classId, String keyword, List<Object> params) {
        StringBuilder sb = new StringBuilder("WHERE c.teacher_id = ? ");
        params.add(teacherId);
        if (classId != null) {
            sb.append("AND EXISTS (SELECT 1 FROM \"ClassStudent\" x JOIN \"Class\" xc ON xc.class_id = x.class_id "
                    + "WHERE x.student_id = u.user_id AND x.class_id = ? AND xc.teacher_id = ?) ");
            params.add(classId);
            params.add(teacherId);
        }
        if (keyword != null && !keyword.isBlank()) {
            // Ignores case and Vietnamese accents: "nguyen" finds "Nguyễn".
            String like = SearchUtil.likePattern(keyword);
            sb.append("AND (" + SearchUtil.foldSql("u.full_name") + " LIKE ? ESCAPE '\\' "
                    + "  OR " + SearchUtil.foldSql("u.username") + " LIKE ? ESCAPE '\\' "
                    + "  OR " + SearchUtil.foldSql("u.email") + " LIKE ? ESCAPE '\\') ");
            for (int i = 0; i < 3; i++) {
                params.add(SearchUtil.FROM);
                params.add(SearchUtil.TO);
                params.add(like);
            }
        }
        return sb.toString();
    }

    public int countStudents(int teacherId, Integer classId, String keyword) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(DISTINCT u.user_id) " + FROM + where(teacherId, classId, keyword, params);
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public List<StudentRowDTO> getStudents(int teacherId, Integer classId, String keyword, int offset, int limit)
            throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, "
                + "       ARRAY_AGG(DISTINCT c.class_code ORDER BY c.class_code) AS codes "
                + FROM + where(teacherId, classId, keyword, params)
                + "GROUP BY u.user_id, u.username, u.full_name, u.email "
                + "ORDER BY u.user_id LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<StudentRowDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    Array codes = r.getArray("codes");
                    list.add(new StudentRowDTO(r.getInt("user_id"), r.getString("username"), r.getString("full_name"),
                            r.getString("email"), codes == null ? null : Arrays.asList((String[]) codes.getArray())));
                }
            }
        }
        return list;
    }

    public List<StudentClassDTO> getEnrolments(int teacherId, int studentId) throws SQLException {
        String sql = "SELECT c.class_id, c.class_code, c.semester, c.year, s.name AS subject_name, s.value AS subject_code, "
                + "       cs.enroll_date, "
                + "       (SELECT MIN(gs.group_id) FROM \"GroupStudent\" gs JOIN \"Group\" g ON g.group_id = gs.group_id "
                + "        WHERE g.class_id = c.class_id AND gs.user_id = cs.student_id) AS group_id "
                + "FROM \"ClassStudent\" cs "
                + "JOIN \"Class\" c ON c.class_id = cs.class_id "
                + "LEFT JOIN \"Settings\" s ON s.settingid = c.subject_id "
                + "WHERE cs.student_id = ? AND c.teacher_id = ? "
                + "ORDER BY c.year DESC, "
                + "  CASE UPPER(LEFT(c.semester, 2)) WHEN 'FA' THEN 3 WHEN 'SU' THEN 2 WHEN 'SP' THEN 1 ELSE 0 END DESC, "
                + "  c.class_code";
        List<StudentClassDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, studentId);
            st.setInt(2, teacherId);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    Timestamp ts = r.getTimestamp("enroll_date");
                    list.add(new StudentClassDTO(r.getInt("class_id"), r.getString("class_code"),
                            r.getString("subject_name"), r.getString("subject_code"),
                            r.getString("semester"), r.getInt("year"),
                            ts == null ? null : ts.toLocalDateTime(), r.getInt("group_id")));
                }
            }
        }
        return list;
    }

    public User findUser(int userId) throws SQLException {
        String sql = "SELECT user_id, role_id, username, email, full_name, status FROM \"User\" WHERE user_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, userId);
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

    private static void bind(PreparedStatement st, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }
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
