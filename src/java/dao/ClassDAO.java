package dao;

import dto.ClassDTO;
import dto.ClassStudentDTO;
import dto.SemesterOptionDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Class;
import model.Setting;
import model.User;
import utils.SemesterUtil;

public class ClassDAO extends BaseDAO implements AutoCloseable {

    private static final String CLASS_SELECT
            = "SELECT c.class_id, c.subject_id, c.teacher_id, c.class_code, c.semester, c.year, "
            + "       s.name AS subject_name, s.value AS subject_code, "
            + "       t.full_name AS teacher_name, t.username AS teacher_username, "
            + "       (SELECT COUNT(*) FROM \"ClassStudent\" cs WHERE cs.class_id = c.class_id) AS student_count, "
            + "       (SELECT COUNT(*) FROM \"Group\" g WHERE g.class_id = c.class_id) AS group_count, "
            + "       (SELECT COUNT(*) FROM \"ClassAssignment\" a WHERE a.class_id = c.class_id) AS assignment_count "
            + "FROM \"Class\" c "
            + "LEFT JOIN \"Settings\" s ON s.settingid = c.subject_id "
            + "LEFT JOIN \"User\" t ON t.user_id = c.teacher_id ";

    private static final String CLASS_ORDER
            = "ORDER BY c.class_id";

    public ClassDAO() throws SQLException {
        super();
        if (con == null) {
            throw new SQLException("Cannot connect to the database (check BaseDAO settings).");
        }
    }

    public static class ClassFilter {

        public Integer subjectId;
        public String semesterPrefix;
        public Integer year;
        public String keyword;
        public Integer teacherId;
        public Integer expertId;
    }

    public int countClasses(ClassFilter f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM \"Class\" c " + where(f, params);
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public List<ClassDTO> getClasses(ClassFilter f, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = CLASS_SELECT + where(f, params) + CLASS_ORDER + " LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<ClassDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(mapClassRow(r));
                }
            }
        }
        return list;
    }

    private static String where(ClassFilter f, List<Object> params) {
        StringBuilder sb = new StringBuilder("WHERE 1 = 1 ");
        if (f.subjectId != null) {
            sb.append("AND c.subject_id = ? ");
            params.add(f.subjectId);
        }
        if (f.semesterPrefix != null) {
            sb.append("AND UPPER(LEFT(c.semester, 2)) = ? ");
            params.add(f.semesterPrefix);
        }
        if (f.year != null) {
            sb.append("AND c.year = ? ");
            params.add(f.year);
        }
        if (f.keyword != null && !f.keyword.isBlank()) {
            sb.append("AND c.class_code ILIKE ? ESCAPE '\\' ");
            params.add("%" + escapeLike(f.keyword.trim()) + "%");
        }
        if (f.teacherId != null) {
            sb.append("AND c.teacher_id = ? ");
            params.add(f.teacherId);
        }
        if (f.expertId != null) {
            sb.append("AND c.subject_id IN (SELECT se.subject_id FROM \"SubjectExpert\" se WHERE se.user_id = ?) ");
            params.add(f.expertId);
        }
        return sb.toString();
    }

    public List<SemesterOptionDTO> getSemesterOptions(Integer teacherId) throws SQLException {
        return getSemesterOptions(teacherId, null);
    }

    public List<SemesterOptionDTO> getSemesterOptions(Integer teacherId, Integer expertId) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE 1 = 1 ");
        if (teacherId != null) {
            where.append("AND c.teacher_id = ? ");
            params.add(teacherId);
        }
        if (expertId != null) {
            where.append("AND c.subject_id IN (SELECT se.subject_id FROM \"SubjectExpert\" se WHERE se.user_id = ?) ");
            params.add(expertId);
        }
        String sql = "SELECT DISTINCT c.semester, c.year, "
                + "CASE UPPER(LEFT(c.semester, 2)) WHEN 'FA' THEN 3 WHEN 'SU' THEN 2 WHEN 'SP' THEN 1 ELSE 0 END AS term_order "
                + "FROM \"Class\" c "
                + where
                + "ORDER BY c.year DESC, term_order DESC";
        List<SemesterOptionDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    String raw = r.getString("semester");
                    String semester = SemesterUtil.normalize(raw);
                    if (semester == null) {
                        continue; // not Spring/Summer/Fall: cannot be filtered on anyway
                    }
                    SemesterOptionDTO opt = new SemesterOptionDTO(semester, r.getInt("year"));
                    // Old data may hold "FALL" and "Fall" for the same term: keep one entry.
                    boolean seen = list.stream().anyMatch(o -> o.getValue().equals(opt.getValue()));
                    if (!seen) {
                        list.add(opt);
                    }
                }
            }
        }
        return list;
    }

    // ------------------------------------------------------------------ Class Detail
    /**
     * Class with subject/teacher names and counts, or null.
     */
    public ClassDTO findClassById(int classId) throws SQLException {
        String sql = CLASS_SELECT + "WHERE c.class_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? mapClassRow(r) : null;
            }
        }
    }

    public Class findClassByCode(String classCode) throws SQLException {
        String sql = "SELECT class_id, subject_id, teacher_id, class_code, semester, year "
                + "FROM \"Class\" WHERE UPPER(class_code) = UPPER(?)";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, classCode);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? mapClass(r) : null;
            }
        }
    }

    public Class createClass(Class c) throws SQLException {
        String sql = "INSERT INTO \"Class\"(subject_id, teacher_id, class_code, semester, year) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, c.getSubjectId());
            st.setInt(2, c.getTeacherId());
            st.setString(3, c.getClassCode());
            st.setString(4, c.getSemester());
            st.setInt(5, c.getYear());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt("class_id"));
                }
            }
        }
        return c;
    }

    public Class updateClass(int classId, Class c) throws SQLException {
        String sql = "UPDATE \"Class\" SET subject_id = ?, teacher_id = ?, class_code = ?, semester = ?, year = ? "
                + "WHERE class_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, c.getSubjectId());
            st.setInt(2, c.getTeacherId());
            st.setString(3, c.getClassCode());
            st.setString(4, c.getSemester());
            st.setInt(5, c.getYear());
            st.setInt(6, classId);
            if (st.executeUpdate() == 0) {
                return null;
            }
        }
        c.setId(classId);
        return c;
    }

    public boolean deleteClass(int classId) throws SQLException {
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            // Lock the class row so nobody adds a group/assignment between the check and the delete.
            try (PreparedStatement st = con.prepareStatement(
                    "SELECT 1 FROM \"Class\" WHERE class_id = ? FOR UPDATE")) {
                st.setInt(1, classId);
                try (ResultSet r = st.executeQuery()) {
                    if (!r.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }
            try (PreparedStatement st = con.prepareStatement(
                    "SELECT (SELECT COUNT(*) FROM \"Group\" WHERE class_id = ?) "
                    + "     + (SELECT COUNT(*) FROM \"ClassAssignment\" WHERE class_id = ?)")) {
                st.setInt(1, classId);
                st.setInt(2, classId);
                try (ResultSet r = st.executeQuery()) {
                    if (r.next() && r.getInt(1) > 0) {
                        con.rollback();
                        return false;
                    }
                }
            }
            try (PreparedStatement st = con.prepareStatement("DELETE FROM \"ClassStudent\" WHERE class_id = ?")) {
                st.setInt(1, classId);
                st.executeUpdate();
            }
            int deleted;
            try (PreparedStatement st = con.prepareStatement("DELETE FROM \"Class\" WHERE class_id = ?")) {
                st.setInt(1, classId);
                deleted = st.executeUpdate();
            }
            con.commit();
            return deleted > 0;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    // ------------------------------------------------------------------ Lookups (dropdowns, roles)

    /**
     * Subjects from the Settings table (type = Subject), for the Subject
     * dropdowns.
     */
    public List<Setting> getSubjects() throws SQLException {
        String sql = "SELECT settingid, name, type, value, description, priority, status "
                + "FROM \"Settings\" WHERE UPPER(type) = 'SUBJECT' AND UPPER(status) = 'ACTIVE' "
                + "ORDER BY COALESCE(NULLIF(value, ''), name)";
        List<Setting> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql); ResultSet r = st.executeQuery()) {
            while (r.next()) {
                list.add(new Setting(r.getInt("settingid"), r.getString("name"), r.getString("type"),
                        r.getString("value"), r.getString("description"), r.getInt("priority"),
                        r.getString("status")));
            }
        }
        return list;
    }

    /**
     * Subjects managed by a specific Expert (from the SubjectExpert table).
     */
    public List<Setting> getSubjectsForExpert(int expertId) throws SQLException {
        String sql = "SELECT s.settingid, s.name, s.type, s.value, s.description, s.priority, s.status "
                + "FROM \"Settings\" s "
                + "JOIN \"SubjectExpert\" se ON se.subject_id = s.settingid "
                + "WHERE UPPER(s.type) = 'SUBJECT' AND UPPER(s.status) = 'ACTIVE' AND se.user_id = ? "
                + "ORDER BY COALESCE(NULLIF(s.value, ''), s.name)";
        List<Setting> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, expertId);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(new Setting(r.getInt("settingid"), r.getString("name"), r.getString("type"),
                            r.getString("value"), r.getString("description"), r.getInt("priority"),
                            r.getString("status")));
                }
            }
        }
        return list;
    }

    /**
     * Subjects of the classes taught by a specific Teacher.
     */
    public List<Setting> getSubjectsForTeacher(int teacherId) throws SQLException {
        String sql = "SELECT DISTINCT s.settingid, s.name, s.type, s.value, s.description, s.priority, s.status, "
                + "COALESCE(NULLIF(s.value, ''), s.name) AS sort_key "
                + "FROM \"Settings\" s "
                + "JOIN \"Class\" c ON c.subject_id = s.settingid "
                + "WHERE UPPER(s.type) = 'SUBJECT' AND UPPER(s.status) = 'ACTIVE' AND c.teacher_id = ? "
                + "ORDER BY sort_key";
        List<Setting> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, teacherId);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(new Setting(r.getInt("settingid"), r.getString("name"), r.getString("type"),
                            r.getString("value"), r.getString("description"), r.getInt("priority"),
                            r.getString("status")));
                }
            }
        }
        return list;
    }

    /**
     * True if the id is a subject in Settings.
     */
    public boolean isSubject(int settingId) throws SQLException {
        String sql = "SELECT 1 FROM \"Settings\" WHERE settingid = ? AND UPPER(type) = 'SUBJECT' AND UPPER(status) = 'ACTIVE'";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, settingId);
            try (ResultSet r = st.executeQuery()) {
                return r.next();
            }
        }
    }

    /**
     * True if the subject is assigned to this Expert in SubjectExpert.
     */
    public boolean isSubjectOfExpert(int settingId, int expertId) throws SQLException {
        String sql = "SELECT 1 FROM \"Settings\" s "
                + "JOIN \"SubjectExpert\" se ON se.subject_id = s.settingid "
                + "WHERE s.settingid = ? AND UPPER(s.type) = 'SUBJECT' AND UPPER(s.status) = 'ACTIVE' AND se.user_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, settingId);
            st.setInt(2, expertId);
            try (ResultSet r = st.executeQuery()) {
                return r.next();
            }
        }
    }

    /**
     * Users whose role is Teacher, by name (for the Teacher dropdown).
     */
    public List<User> getUsersByRoleName(String roleName) throws SQLException {
        String sql = "SELECT u.user_id, u.role_id, u.username, u.email, u.full_name, u.status "
                + "FROM \"User\" u JOIN \"Settings\" r ON r.settingid = u.role_id "
                + "WHERE UPPER(r.name) = UPPER(?) "
                + "ORDER BY COALESCE(NULLIF(u.full_name, ''), u.username)";
        List<User> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, roleName);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(mapUser(r));
                }
            }
        }
        return list;
    }

    /**
     * User by id, or null.
     */
    public User findUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, role_id, username, email, full_name, status FROM \"User\" WHERE user_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? mapUser(r) : null;
            }
        }
    }

    /**
     * Role name stored in Settings for this role id (e.g. "Teacher"), or null.
     */
    public String findRoleName(int roleId) throws SQLException {
        String sql = "SELECT name FROM \"Settings\" WHERE settingid = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, roleId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getString("name") : null;
            }
        }
    }

    // ------------------------------------------------------------------ Student roster
    public int countStudents(int classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"ClassStudent\" WHERE class_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    /**
     * One page of the roster, sorted by student id.
     */
    public List<ClassStudentDTO> getStudents(int classId, int offset, int limit) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, cs.enroll_date "
                + "FROM \"ClassStudent\" cs JOIN \"User\" u ON u.user_id = cs.student_id "
                + "WHERE cs.class_id = ? "
                + "ORDER BY u.user_id "
                + "LIMIT ? OFFSET ?";
        List<ClassStudentDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            st.setInt(2, limit);
            st.setInt(3, offset);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    Timestamp ts = r.getTimestamp("enroll_date");
                    list.add(new ClassStudentDTO(r.getInt("user_id"), r.getString("username"),
                            r.getString("full_name"), r.getString("email"),
                            ts == null ? null : ts.toLocalDateTime()));
                }
            }
        }
        return list;
    }

    /**
     * Students (role Student) matching what the teacher typed: exact username
     * or email, or the exact full name (ignoring case). Can return several
     * people when names are the same.
     */
    public List<User> findStudents(String query, String studentRoleName) throws SQLException {
        String sql = "SELECT u.user_id, u.role_id, u.username, u.email, u.full_name, u.status "
                + "FROM \"User\" u JOIN \"Settings\" r ON r.settingid = u.role_id "
                + "WHERE UPPER(r.name) = UPPER(?) "
                + "  AND (LOWER(u.username) = LOWER(?) OR LOWER(u.email) = LOWER(?) OR LOWER(u.full_name) = LOWER(?)) "
                + "ORDER BY u.user_id LIMIT 10";
        List<User> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setString(1, studentRoleName);
            st.setString(2, query);
            st.setString(3, query);
            st.setString(4, query);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(mapUser(r));
                }
            }
        }
        return list;
    }

    public boolean isEnrolled(int classId, int studentId) throws SQLException {
        String sql = "SELECT 1 FROM \"ClassStudent\" WHERE class_id = ? AND student_id = ?";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            st.setInt(2, studentId);
            try (ResultSet r = st.executeQuery()) {
                return r.next();
            }
        }
    }

    /**
     * Enrols the student; returns false if they were already in the class.
     */
    public boolean addStudent(int classId, int studentId) throws SQLException {
        String sql = "INSERT INTO \"ClassStudent\"(class_id, student_id, enroll_date) VALUES (?, ?, NOW()) "
                + "ON CONFLICT (class_id, student_id) DO NOTHING";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            st.setInt(2, studentId);
            return st.executeUpdate() > 0;
        }
    }

    /**
     * Takes the student out of the class and out of any group of that class, in
     * one transaction. Their submissions are kept. Returns false if they were
     * not in the class.
     */
    public boolean removeStudent(int classId, int studentId) throws SQLException {
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            try (PreparedStatement st = con.prepareStatement(
                    "DELETE FROM \"GroupStudent\" WHERE user_id = ? "
                    + "AND group_id IN (SELECT group_id FROM \"Group\" WHERE class_id = ?)")) {
                st.setInt(1, studentId);
                st.setInt(2, classId);
                st.executeUpdate();
            }
            int removed;
            try (PreparedStatement st = con.prepareStatement(
                    "DELETE FROM \"ClassStudent\" WHERE class_id = ? AND student_id = ?")) {
                st.setInt(1, classId);
                st.setInt(2, studentId);
                removed = st.executeUpdate();
            }
            con.commit();
            return removed > 0;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    // ------------------------------------------------------------------ helpers
    private static ClassDTO mapClassRow(ResultSet r) throws SQLException {
        ClassDTO d = new ClassDTO(r.getInt("class_id"), r.getInt("subject_id"), r.getInt("teacher_id"),
                r.getString("class_code"), r.getString("semester"), r.getInt("year"));
        d.setSubjectName(r.getString("subject_name"));
        d.setSubjectCode(r.getString("subject_code"));
        d.setTeacherName(r.getString("teacher_name"));
        d.setTeacherUsername(r.getString("teacher_username"));
        d.setStudentCount(r.getInt("student_count"));
        d.setGroupCount(r.getInt("group_count"));
        d.setAssignmentCount(r.getInt("assignment_count"));
        return d;
    }

    private static Class mapClass(ResultSet r) throws SQLException {
        return new Class(r.getInt("class_id"), r.getInt("subject_id"), r.getInt("teacher_id"),
                r.getString("class_code"), r.getString("semester"), r.getInt("year"));
    }

    private static User mapUser(ResultSet r) throws SQLException {
        User u = new User();
        u.setId(r.getInt("user_id"));
        u.setRoleId(r.getInt("role_id"));
        u.setUsername(r.getString("username"));
        u.setEmail(r.getString("email"));
        u.setFullName(r.getString("full_name"));
        u.setStatus(r.getString("status"));
        return u;
    }

    private static void bind(PreparedStatement st, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            st.setObject(i + 1, params.get(i));
        }
    }

    /**
     * Makes % and _ in the keyword match literally in ILIKE.
     */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
    
    /**
     * Đếm số class theo từng subject — dùng cho admin dashboard.
     * Đếm số class theo từng subject — dùng cho admin dashboard.
     * Trả về list Object[]: [0] = subjectId (int), [1] = subjectName (String), [2] = classCount (int)
     */
    public List<Object[]> countClassesBySubject() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        xSql = "SELECT s.settingid AS subject_id, s.name AS subject_name, COUNT(c.class_id) AS class_count "
             + "FROM \"Settings\" s "
             + "LEFT JOIN \"Class\" c ON c.subject_id = s.settingid "
             + "WHERE UPPER(s.type) = 'SUBJECT' AND s.status = 'Active' "
             + "GROUP BY s.settingid, s.name "
             + "ORDER BY class_count DESC, s.name";
        try {
            ps = con.prepareStatement(xSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("subject_id"),
                    rs.getString("subject_name"),
                    rs.getInt("class_count")
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
