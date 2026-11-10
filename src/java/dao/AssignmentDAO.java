/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dto.AssignmentDTO;
import dto.AssignmentTemplateDTO;
import dto.SemesterOptionDTO;
import model.Setting;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Assignment;
import model.Class;

/**
 * SQL for the teacher dashboard and assignment screens.
 *
 * Queries that depend on filters are built in small steps: every time a "?" is added
 * to the SQL, its value is added to the params list, so the two always stay in the same order.
 *
 * Common parameters: admin = true means "every class", otherwise only the classes of userId.
 * term = null means "every term".
 */
public class AssignmentDAO extends BaseDAO implements AutoCloseable {

    // Used together with readAssignment(): a = ClassAssignment, c = Class, s = Settings (the subject)
    private static final String ASSIGNMENT_COLUMNS = "a.assignment_id, a.title, a.instruction, a.start_date, a.due_date, "
            + "a.status, c.class_id, c.class_code, c.semester, c.year, s.name AS subject_name ";
    private static final String ASSIGNMENT_TABLES = "FROM \"ClassAssignment\" a "
            + "JOIN \"Class\" c ON c.class_id = a.class_id "
            + "LEFT JOIN \"Settings\" s ON s.settingid = c.subject_id ";

    public AssignmentDAO() throws SQLException {
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

    // ----- Current term, classes, subjects, templates -----

    /**
     * The current term of a teacher is the newest term they have a class in:
     * the highest year, and within a year Fall > Summer > Spring. Null if the teacher has no class.
     */
    public SemesterOptionDTO findCurrentTerm(int teacherId) throws SQLException {
        String sql = "SELECT semester, year FROM \"Class\" WHERE teacher_id = ? "
                + "ORDER BY year DESC, "
                + "CASE UPPER(semester) WHEN 'FALL' THEN 3 WHEN 'SUMMER' THEN 2 WHEN 'SPRING' THEN 1 ELSE 0 END DESC "
                + "LIMIT 1";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new SemesterOptionDTO(result.getString("semester"), result.getInt("year"));
                }
                return null;
            }
        }
    }

    public List<Class> findClasses(int userId, boolean admin, SemesterOptionDTO term) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT c.class_id, c.subject_id, c.teacher_id, c.class_code, c.semester, c.year, s.name AS subject_name "
                + "FROM \"Class\" c LEFT JOIN \"Settings\" s ON s.settingid = c.subject_id "
                + "WHERE 1 = 1 "
                + teacherAndTermConditions(params, userId, admin, term)
                + "ORDER BY c.year DESC, c.class_code";
        List<Class> classes = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Class classItem = new Class(
                            result.getInt("class_id"),
                            result.getInt("subject_id"),
                            result.getInt("teacher_id"),
                            result.getString("class_code"),
                            result.getString("semester"),
                            result.getInt("year"));
                    classItem.setSubjectName(result.getString("subject_name"));
                    classes.add(classItem);
                }
            }
        }
        return classes;
    }

    public List<Setting> findSubjects(int userId, boolean admin, SemesterOptionDTO term) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT DISTINCT s.settingid, s.name, s.type, s.value, s.description, s.priority, s.status "
                + "FROM \"Settings\" s JOIN \"Class\" c ON c.subject_id = s.settingid "
                + "WHERE 1 = 1 "
                + teacherAndTermConditions(params, userId, admin, term)
                + "ORDER BY s.name";
        List<Setting> subjects = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    subjects.add(new Setting(
                            result.getInt("settingid"),
                            result.getString("name"),
                            result.getString("type"),
                            result.getString("value"),
                            result.getString("description"),
                            result.getInt("priority"),
                            result.getString("status")));
                }
            }
        }
        return subjects;
    }

    /** Templates of the subjects the user teaches. */
    public List<AssignmentTemplateDTO> findTemplates(int userId, boolean admin) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT t.template_id, t.subject_id, s.name AS subject_name, t.title, t.instruction "
                + "FROM \"AssignmentTemplate\" t JOIN \"Settings\" s ON s.settingid = t.subject_id "
                + "WHERE EXISTS (SELECT 1 FROM \"Class\" c WHERE c.subject_id = t.subject_id "
                + teacherAndTermConditions(params, userId, admin, null)
                + ") ORDER BY s.name, t.title";
        List<AssignmentTemplateDTO> templates = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    AssignmentTemplateDTO template = new AssignmentTemplateDTO();
                    template.setTemplateId(result.getInt("template_id"));
                    template.setSubjectId(result.getInt("subject_id"));
                    template.setSubjectName(result.getString("subject_name"));
                    template.setTitle(result.getString("title"));
                    template.setInstruction(result.getString("instruction"));
                    templates.add(template);
                }
            }
        }
        return templates;
    }

    // ----- Assignment List and dashboard -----

    public int countAssignments(int userId, boolean admin, SemesterOptionDTO term, Integer subjectId,
            Integer classId, String status, String keyword) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) " + ASSIGNMENT_TABLES
                + listWhere(params, userId, admin, term, subjectId, classId, status, keyword);
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    /** One page of the Assignment List, newest first. */
    public List<AssignmentDTO> findAssignments(int userId, boolean admin, SemesterOptionDTO term, Integer subjectId,
            Integer classId, String status, String keyword, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT " + ASSIGNMENT_COLUMNS + ASSIGNMENT_TABLES
                + listWhere(params, userId, admin, term, subjectId, classId, status, keyword)
                + "ORDER BY a.start_date DESC, a.assignment_id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        return readAssignments(sql, params);
    }

    /** The teacher's latest assignments of any term, for the dashboard. */
    public List<AssignmentDTO> findRecentAssignments(int teacherId, int limit) throws SQLException {
        String sql = "SELECT " + ASSIGNMENT_COLUMNS + ASSIGNMENT_TABLES
                + "WHERE c.teacher_id = ? "
                + "ORDER BY a.start_date DESC, a.assignment_id DESC LIMIT ?";
        List<Object> params = new ArrayList<>();
        params.add(teacherId);
        params.add(limit);
        return readAssignments(sql, params);
    }

    /** Published assignments of the teacher with ungraded submissions, most pending first. */
    public List<AssignmentDTO> findAssignmentsToEvaluate(int teacherId) throws SQLException {
        String sql = "SELECT " + ASSIGNMENT_COLUMNS + ", COUNT(sub.submission_id) AS pending_submission_count "
                + ASSIGNMENT_TABLES
                + "JOIN \"Submission\" sub ON sub.assignment_id = a.assignment_id "
                + "WHERE c.teacher_id = ? AND UPPER(a.status) = 'PUBLISHED' "
                + "AND sub.score IS NULL AND UPPER(sub.status) = 'SUBMITTED' "
                + "GROUP BY a.assignment_id, a.title, a.instruction, a.start_date, a.due_date, a.status, "
                + "c.class_id, c.class_code, c.semester, c.year, s.name "
                + "ORDER BY pending_submission_count DESC, a.due_date ASC LIMIT 5";
        List<AssignmentDTO> assignments = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, teacherId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    AssignmentDTO assignment = readAssignment(result);
                    assignment.setPendingSubmissionCount(result.getInt("pending_submission_count"));
                    assignments.add(assignment);
                }
            }
        }
        return assignments;
    }

    /** The assignment if the user may manage it, otherwise null. */
    public AssignmentDTO findAssignmentById(int assignmentId, int userId, boolean admin) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT " + ASSIGNMENT_COLUMNS + ASSIGNMENT_TABLES + "WHERE a.assignment_id = ? ";
        params.add(assignmentId);
        sql += teacherAndTermConditions(params, userId, admin, null);
        List<AssignmentDTO> assignments = readAssignments(sql, params);
        if (assignments.isEmpty()) {
            return null;
        }
        return assignments.get(0);
    }

    // ----- Create and update -----

    public boolean canManageClass(int classId, int userId, boolean admin) throws SQLException {
        String sql = "SELECT 1 FROM \"Class\" WHERE class_id = ?";
        if (!admin) {
            sql += " AND teacher_id = ?";
        }
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, classId);
            if (!admin) {
                statement.setInt(2, userId);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /** True if the template exists and belongs to the subject of the class. */
    public boolean templateMatchesClass(int templateId, int classId) throws SQLException {
        String sql = "SELECT 1 FROM \"AssignmentTemplate\" t JOIN \"Class\" c ON c.subject_id = t.subject_id "
                + "WHERE t.template_id = ? AND c.class_id = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, templateId);
            statement.setInt(2, classId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /** Inserts the assignment and returns its new id. */
    public int createAssignment(Assignment assignment) throws SQLException {
        String sql = "INSERT INTO \"ClassAssignment\" "
                + "(class_id, template_id, title, instruction, start_date, due_date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING assignment_id";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, assignment.getClassId());
            if (assignment.getTemplateId() == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, assignment.getTemplateId());
            }
            statement.setString(3, assignment.getTitle());
            statement.setString(4, assignment.getInstruction());
            statement.setTimestamp(5, Timestamp.valueOf(assignment.getStartDate()));
            statement.setTimestamp(6, Timestamp.valueOf(assignment.getDueDate()));
            statement.setString(7, assignment.getStatus());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt("assignment_id");
            }
        }
    }

    public boolean updateAssignment(Assignment assignment, int userId, boolean admin) throws SQLException {
        String sql = "UPDATE \"ClassAssignment\" a SET title = ?, instruction = ?, start_date = ?, due_date = ?, status = ? "
                + "FROM \"Class\" c WHERE c.class_id = a.class_id AND a.assignment_id = ?";
        if (!admin) {
            sql += " AND c.teacher_id = ?";
        }
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, assignment.getTitle());
            statement.setString(2, assignment.getInstruction());
            statement.setTimestamp(3, Timestamp.valueOf(assignment.getStartDate()));
            statement.setTimestamp(4, Timestamp.valueOf(assignment.getDueDate()));
            statement.setString(5, assignment.getStatus());
            statement.setInt(6, assignment.getAssignmentId());
            if (!admin) {
                statement.setInt(7, userId);
            }
            return statement.executeUpdate() > 0;
        }
    }

    /** True if another assignment of the class already has this title (case and outer spaces ignored). */
    public boolean titleExists(int classId, String title, int excludeAssignmentId) throws SQLException {
        String sql = "SELECT 1 FROM \"ClassAssignment\" WHERE class_id = ? "
                + "AND LOWER(TRIM(title)) = LOWER(TRIM(?)) AND assignment_id <> ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setInt(1, classId);
            statement.setString(2, title);
            statement.setInt(3, excludeAssignmentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // ----- Helpers -----

    // "This teacher only" (skipped for admins) and "this term only" (skipped when term is null).
    private static String teacherAndTermConditions(List<Object> params, int userId, boolean admin, SemesterOptionDTO term) {
        String conditions = "";
        if (!admin) {
            conditions += "AND c.teacher_id = ? ";
            params.add(userId);
        }
        if (term != null) {
            conditions += "AND c.year = ? AND UPPER(c.semester) = UPPER(?) ";
            params.add(term.getYear());
            params.add(term.getSemester());
        }
        return conditions;
    }

    // WHERE part of the Assignment List: each filter is only applied when the user filled it in.
    private static String listWhere(List<Object> params, int userId, boolean admin, SemesterOptionDTO term,
            Integer subjectId, Integer classId, String status, String keyword) {
        String where = "WHERE 1 = 1 " + teacherAndTermConditions(params, userId, admin, term);
        if (subjectId != null) {
            where += "AND c.subject_id = ? ";
            params.add(subjectId);
        }
        if (classId != null) {
            where += "AND c.class_id = ? ";
            params.add(classId);
        }
        if (status != null && !status.isBlank()) {
            where += "AND a.status = ? ";
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String search = "%" + keyword.trim() + "%";
            where += "AND (a.title ILIKE ? OR a.instruction ILIKE ?) ";
            params.add(search);
            params.add(search);
        }
        return where;
    }

    private static void setParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private List<AssignmentDTO> readAssignments(String sql, List<Object> params) throws SQLException {
        List<AssignmentDTO> assignments = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            setParams(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    assignments.add(readAssignment(result));
                }
            }
        }
        return assignments;
    }

    private static AssignmentDTO readAssignment(ResultSet result) throws SQLException {
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
        return assignment;
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
