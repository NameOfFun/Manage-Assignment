package dto;

import java.util.ArrayList;
import java.util.List;
import model.User;

public class AdminDashboardDTO {

    private String fullName;
    private int totalUsers;
    private int activeUsers;
    private int inactiveUsers;
    private int totalSubjects;
    private int activeSubjects;
    private int inactiveSubjects;
    private int totalClasses;
    // Classes per subject: each Object[] = { String subjectName, int classCount }
    private List<Object[]> classesPerSubject = new ArrayList<>();
    // Users by role for donut chart
    private int adminCount;
    private int expertCount;
    private int teacherCount;
    private int studentCount;
    private List<User> users = new ArrayList<>();

    public AdminDashboardDTO() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }

    public int getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }

    public int getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(int totalSubjects) { this.totalSubjects = totalSubjects; }

    public int getActiveSubjects() { return activeSubjects; }
    public void setActiveSubjects(int activeSubjects) { this.activeSubjects = activeSubjects; }

    public int getInactiveSubjects() { return inactiveSubjects; }
    public void setInactiveSubjects(int inactiveSubjects) { this.inactiveSubjects = inactiveSubjects; }

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    public List<Object[]> getClassesPerSubject() { return classesPerSubject; }
    public void setClassesPerSubject(List<Object[]> classesPerSubject) { this.classesPerSubject = classesPerSubject; }

    public int getAdminCount() { return adminCount; }
    public void setAdminCount(int adminCount) { this.adminCount = adminCount; }

    public int getExpertCount() { return expertCount; }
    public void setExpertCount(int expertCount) { this.expertCount = expertCount; }

    public int getTeacherCount() { return teacherCount; }
    public void setTeacherCount(int teacherCount) { this.teacherCount = teacherCount; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

}
