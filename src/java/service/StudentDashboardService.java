package service;

import dao.StudentDashboardDAO;
import dto.StudentDashboardDTO;
import java.sql.SQLException;
import model.User;

/**
 * Builds the data for the Student Dashboard (screen "Student Dashboard", Iter2).
 */
public class StudentDashboardService {

    /** Role name in the Settings table that marks a student account. */
    public static final String STUDENT_ROLE = "Student";

    private static final int UPCOMING_LIMIT = 5;
    private static final int RECENT_GRADES_LIMIT = 5;

    /**
     * Loads the dashboard for the logged-in user.
     *
     * @return null if the username does not exist; a DTO with {@code isStudent() == false}
     * if the account is not a student (the servlet answers 403 in that case).
     */
    public StudentDashboardDTO getDashboard(String username) throws SQLException {
        try (StudentDashboardDAO dao = new StudentDashboardDAO()) {
            User user = dao.findUserByUsername(username);
            if (user == null) {
                return null;
            }

            StudentDashboardDTO dash = new StudentDashboardDTO();
            dash.setStudentId(user.getId());
            dash.setFullName(user.getFullName());
            dash.setStudent(STUDENT_ROLE.equalsIgnoreCase(dao.findRoleName(user.getRoleId())));
            if (!dash.isStudent()) {
                return dash;
            }

            int id = user.getId();
            dash.setClasses(dao.findClasses(id));
            dash.setUpcoming(dao.findUpcomingAssignments(id, UPCOMING_LIMIT));
            dash.setPendingCount(dao.countPendingAssignments(id));
            dash.setSubmittedCount(dao.countSubmittedAssignments(id));
            dash.setRecentGrades(dao.findRecentGrades(id, RECENT_GRADES_LIMIT));
            return dash;
        }
    }
}
