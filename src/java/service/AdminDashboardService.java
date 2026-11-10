package service;

import dao.ClassDAO;
import dao.SettingDAO;
import dao.UserDAO;
import dto.AdminDashboardDTO;
import model.User;

public class AdminDashboardService {

    private final UserDAO userDAO = new UserDAO();
    private final SettingDAO settingDAO = new SettingDAO();

    public AdminDashboardDTO getDashboard(String username) {
        User admin = userDAO.getUser(username);
        if (admin == null) {
            return null;
        }

        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setFullName(admin.getFullName());
        dto.setTotalUsers(userDAO.countUsers());
        dto.setActiveUsers(userDAO.countUsersByRoleAndStatus(null, "Active"));
        dto.setInactiveUsers(userDAO.countUsersByRoleAndStatus(null, "Inactive"));
        dto.setTotalSubjects(settingDAO.countByType("Subject"));
        dto.setActiveSubjects(settingDAO.countByTypeAndStatus("Subject", "Active"));
        dto.setInactiveSubjects(settingDAO.countByTypeAndStatus("Subject", "Inactive"));
        // Users by role cho donut chart — join Settings theo tên, không hardcode id
        dto.setAdminCount(userDAO.countUsersByRoleName("Admin"));
        dto.setExpertCount(userDAO.countUsersByRoleName("Expert"));
        dto.setTeacherCount(userDAO.countUsersByRoleName("Teacher"));
        dto.setStudentCount(userDAO.countUsersByRoleName("Student"));
        // Class stats
        try {
            ClassDAO classDAO = new ClassDAO();
            ClassDAO.ClassFilter all = new ClassDAO.ClassFilter();
            dto.setTotalClasses(classDAO.countClasses(all));
            dto.setClassesPerSubject(classDAO.countClassesBySubject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}
