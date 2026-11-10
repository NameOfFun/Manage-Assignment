package dao;

import dto.ClassStudentDTO;
import dto.GroupRowDTO;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.Group;
import utils.SearchUtil;

public class GroupDAO extends BaseDAO implements AutoCloseable {

    public enum Outcome {
        OK,
        NOT_FOUND,
        IN_USE,
        INVALID_STUDENT,
        IN_OTHER_GROUP
    }

    public static class SaveResult {

        public Outcome outcome;
        public int groupId;
        public List<String> names = new ArrayList<>();
    }

    private static final String DISPLAY_NAME = "COALESCE(NULLIF(u.full_name, ''), u.username)";

    public GroupDAO() throws SQLException {
        super();
        if (con == null) {
            throw new SQLException("Cannot connect to the database (check BaseDAO settings).");
        }
    }

    public int countGroups(int classId, String keyword) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM \"Group\" g " + where(classId, keyword, params);
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public List<GroupRowDTO> getGroups(int classId, String keyword, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT g.group_id, g.class_id, "
                + "       ARRAY(SELECT " + DISPLAY_NAME + " FROM \"GroupStudent\" gs "
                + "             JOIN \"User\" u ON u.user_id = gs.user_id "
                + "             WHERE gs.group_id = g.group_id "
                + "             ORDER BY " + DISPLAY_NAME + ") AS names "
                + "FROM \"Group\" g " + where(classId, keyword, params)
                + "ORDER BY g.group_id LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        List<GroupRowDTO> list = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(sql)) {
            bind(st, params);
            try (ResultSet r = st.executeQuery()) {
                while (r.next()) {
                    list.add(new GroupRowDTO(r.getInt("group_id"), r.getInt("class_id"), toList(r.getArray("names"))));
                }
            }
        }
        return list;
    }

    private static String where(int classId, String keyword, List<Object> params) {
        StringBuilder sb = new StringBuilder("WHERE g.class_id = ? ");
        params.add(classId);
        if (keyword != null && !keyword.isBlank()) {
            // Ignores case and Vietnamese accents: "nguyen" finds "Nguyễn".
            String like = SearchUtil.likePattern(keyword);
            sb.append("AND EXISTS (SELECT 1 FROM \"GroupStudent\" gs JOIN \"User\" u ON u.user_id = gs.user_id "
                    + "WHERE gs.group_id = g.group_id "
                    + "AND (" + SearchUtil.foldSql("u.full_name") + " LIKE ? ESCAPE '\\' "
                    + "  OR " + SearchUtil.foldSql("u.username") + " LIKE ? ESCAPE '\\')) ");
            for (int i = 0; i < 2; i++) {
                params.add(SearchUtil.FROM);
                params.add(SearchUtil.TO);
                params.add(like);
            }
        }
        return sb.toString();
    }

    public int countUnassigned(int classId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"ClassStudent\" cs WHERE cs.class_id = ? " + NOT_IN_A_GROUP;
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            st.setInt(2, classId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    private static final String NOT_IN_A_GROUP
            = "AND NOT EXISTS (SELECT 1 FROM \"GroupStudent\" gs JOIN \"Group\" g ON g.group_id = gs.group_id "
            + "                WHERE g.class_id = ? AND gs.user_id = cs.student_id) ";

    /** The group (id + class id), or null. */
    public Group findGroup(int groupId) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("SELECT group_id, class_id FROM \"Group\" WHERE group_id = ?")) {
            st.setInt(1, groupId);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? new Group(r.getInt("group_id"), r.getInt("class_id")) : null;
            }
        }
    }

    public List<ClassStudentDTO> getMembers(int groupId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, cs.enroll_date "
                + "FROM \"GroupStudent\" gs "
                + "JOIN \"Group\" g ON g.group_id = gs.group_id "
                + "JOIN \"User\" u ON u.user_id = gs.user_id "
                + "LEFT JOIN \"ClassStudent\" cs ON cs.class_id = g.class_id AND cs.student_id = gs.user_id "
                + "WHERE gs.group_id = ? ORDER BY u.user_id";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, groupId);
            return readStudents(st);
        }
    }

    public List<ClassStudentDTO> getUnassigned(int classId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, cs.enroll_date "
                + "FROM \"ClassStudent\" cs JOIN \"User\" u ON u.user_id = cs.student_id "
                + "WHERE cs.class_id = ? " + NOT_IN_A_GROUP
                + "ORDER BY " + DISPLAY_NAME + ", u.user_id";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, classId);
            st.setInt(2, classId);
            return readStudents(st);
        }
    }

    public SaveResult saveGroup(int classId, Integer groupId, List<Integer> studentIds) throws SQLException {
        SaveResult res = new SaveResult();
        Set<Integer> ids = new LinkedHashSet<>(studentIds);
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            if (!lockClass(classId)) {
                con.rollback();
                res.outcome = Outcome.NOT_FOUND;
                return res;
            }
            if (groupId != null) {
                Group g = findGroup(groupId);
                if (g == null || g.getClassId() != classId) {
                    con.rollback();
                    res.outcome = Outcome.NOT_FOUND;
                    return res;
                }
            }
            if (!ids.isEmpty()) {
                Integer[] arr = ids.toArray(new Integer[0]);
                // Everyone must be enrolled in the class.
                try (PreparedStatement st = con.prepareStatement(
                        "SELECT COUNT(*) FROM \"ClassStudent\" WHERE class_id = ? AND student_id = ANY (?)")) {
                    st.setInt(1, classId);
                    st.setArray(2, con.createArrayOf("integer", arr));
                    try (ResultSet r = st.executeQuery()) {
                        if (!r.next() || r.getInt(1) != ids.size()) {
                            con.rollback();
                            res.outcome = Outcome.INVALID_STUDENT;
                            return res;
                        }
                    }
                }
                try (PreparedStatement st = con.prepareStatement(
                        "SELECT " + DISPLAY_NAME + " AS name, gs.group_id FROM \"GroupStudent\" gs "
                        + "JOIN \"Group\" g ON g.group_id = gs.group_id JOIN \"User\" u ON u.user_id = gs.user_id "
                        + "WHERE g.class_id = ? AND gs.group_id <> ? AND gs.user_id = ANY (?) ORDER BY 1")) {
                    st.setInt(1, classId);
                    st.setInt(2, groupId == null ? -1 : groupId);
                    st.setArray(3, con.createArrayOf("integer", arr));
                    try (ResultSet r = st.executeQuery()) {
                        while (r.next()) {
                            res.names.add(r.getString("name") + " (group " + r.getInt("group_id") + ")");
                        }
                    }
                }
                if (!res.names.isEmpty()) {
                    con.rollback();
                    res.outcome = Outcome.IN_OTHER_GROUP;
                    return res;
                }
            }
            int gid;
            if (groupId == null) {
                try (PreparedStatement st = con.prepareStatement(
                        "INSERT INTO \"Group\"(class_id) VALUES (?) RETURNING group_id")) {
                    st.setInt(1, classId);
                    try (ResultSet r = st.executeQuery()) {
                        r.next();
                        gid = r.getInt(1);
                    }
                }
            } else {
                gid = groupId;
                try (PreparedStatement st = con.prepareStatement("DELETE FROM \"GroupStudent\" WHERE group_id = ?")) {
                    st.setInt(1, gid);
                    st.executeUpdate();
                }
            }
            insertMembers(gid, new ArrayList<>(ids));
            con.commit();
            res.outcome = Outcome.OK;
            res.groupId = gid;
            return res;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public Outcome deleteGroup(int classId, int groupId) throws SQLException {
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            try (PreparedStatement st = con.prepareStatement(
                    "SELECT 1 FROM \"Group\" WHERE group_id = ? AND class_id = ? FOR UPDATE")) {
                st.setInt(1, groupId);
                st.setInt(2, classId);
                try (ResultSet r = st.executeQuery()) {
                    if (!r.next()) {
                        con.rollback();
                        return Outcome.NOT_FOUND;
                    }
                }
            }
            if (countUsages("= ?", groupId) > 0) {
                con.rollback();
                return Outcome.IN_USE;
            }
            try (PreparedStatement st = con.prepareStatement("DELETE FROM \"GroupStudent\" WHERE group_id = ?")) {
                st.setInt(1, groupId);
                st.executeUpdate();
            }
            try (PreparedStatement st = con.prepareStatement("DELETE FROM \"Group\" WHERE group_id = ?")) {
                st.setInt(1, groupId);
                st.executeUpdate();
            }
            con.commit();
            return Outcome.OK;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public Outcome randomize(int classId, int count) throws SQLException {
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            if (!lockClass(classId)) {
                con.rollback();
                return Outcome.NOT_FOUND;
            }
            if (countUsages("IN (SELECT group_id FROM \"Group\" WHERE class_id = ?)", classId) > 0) {
                con.rollback();
                return Outcome.IN_USE;
            }
            try (PreparedStatement st = con.prepareStatement(
                    "DELETE FROM \"GroupStudent\" WHERE group_id IN (SELECT group_id FROM \"Group\" WHERE class_id = ?)")) {
                st.setInt(1, classId);
                st.executeUpdate();
            }
            try (PreparedStatement st = con.prepareStatement("DELETE FROM \"Group\" WHERE class_id = ?")) {
                st.setInt(1, classId);
                st.executeUpdate();
            }
            List<Integer> students = new ArrayList<>();
            try (PreparedStatement st = con.prepareStatement("SELECT student_id FROM \"ClassStudent\" WHERE class_id = ?")) {
                st.setInt(1, classId);
                try (ResultSet r = st.executeQuery()) {
                    while (r.next()) {
                        students.add(r.getInt(1));
                    }
                }
            }
            Collections.shuffle(students);
            List<Integer> groupIds = new ArrayList<>();
            try (PreparedStatement st = con.prepareStatement(
                    "INSERT INTO \"Group\"(class_id) VALUES (?) RETURNING group_id")) {
                for (int i = 0; i < count; i++) {
                    st.setInt(1, classId);
                    try (ResultSet r = st.executeQuery()) {
                        r.next();
                        groupIds.add(r.getInt(1));
                    }
                }
            }
            try (PreparedStatement st = con.prepareStatement("INSERT INTO \"GroupStudent\"(user_id, group_id) VALUES (?, ?)")) {
                for (int i = 0; i < students.size(); i++) {
                    st.setInt(1, students.get(i));
                    st.setInt(2, groupIds.get(i % count));
                    st.addBatch();
                }
                st.executeBatch();
            }
            con.commit();
            return Outcome.OK;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    private boolean lockClass(int classId) throws SQLException {
        try (PreparedStatement st = con.prepareStatement("SELECT 1 FROM \"Class\" WHERE class_id = ? FOR UPDATE")) {
            st.setInt(1, classId);
            try (ResultSet r = st.executeQuery()) {
                return r.next();
            }
        }
    }

    private int countUsages(String groupCondition, int param) throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM \"Submission\" WHERE group_id " + groupCondition + ") "
                + "     + (SELECT COUNT(*) FROM \"Scope\" WHERE group_id " + groupCondition + ")";
        try (PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, param);
            st.setInt(2, param);
            try (ResultSet r = st.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    private void insertMembers(int groupId, List<Integer> studentIds) throws SQLException {
        if (studentIds.isEmpty()) {
            return;
        }
        try (PreparedStatement st = con.prepareStatement("INSERT INTO \"GroupStudent\"(user_id, group_id) VALUES (?, ?)")) {
            for (int id : studentIds) {
                st.setInt(1, id);
                st.setInt(2, groupId);
                st.addBatch();
            }
            st.executeBatch();
        }
    }

    private static List<ClassStudentDTO> readStudents(PreparedStatement st) throws SQLException {
        List<ClassStudentDTO> list = new ArrayList<>();
        try (ResultSet r = st.executeQuery()) {
            while (r.next()) {
                Timestamp ts = r.getTimestamp("enroll_date");
                list.add(new ClassStudentDTO(r.getInt("user_id"), r.getString("username"),
                        r.getString("full_name"), r.getString("email"),
                        ts == null ? null : ts.toLocalDateTime()));
            }
        }
        return list;
    }

    private static List<String> toList(Array a) throws SQLException {
        if (a == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList((String[]) a.getArray()));
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
