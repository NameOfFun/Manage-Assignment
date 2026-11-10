package dto;

import java.util.ArrayList;
import java.util.List;


public class StudentDashboardDTO {

    private int studentId;
    private String fullName;
    private boolean student;
    private List<ClassDTO> classes = new ArrayList<>();
    private List<AssignmentDTO> upcoming = new ArrayList<>();
    private List<RecentGradeDTO> recentGrades = new ArrayList<>();
    private int pendingCount;
    private int submittedCount;

    public StudentDashboardDTO() {
    }

    /** First word of the full name, for the greeting ("Welcome back, Minh"). */
    public String getGreetingName() {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.strip().split("\\s+");
        // Vietnamese names put the given name last: "Đỗ Quang Minh" -> "Minh"
        return parts[parts.length - 1];
    }

    /** Two-letter avatar text: "Đỗ Quang Minh" -> "ĐM" */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.strip().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    public int getClassCount() {
        return classes.size();
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isStudent() {
        return student;
    }

    public void setStudent(boolean student) {
        this.student = student;
    }

    public List<ClassDTO> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassDTO> classes) {
        this.classes = classes;
    }

    public List<AssignmentDTO> getUpcoming() {
        return upcoming;
    }

    public void setUpcoming(List<AssignmentDTO> upcoming) {
        this.upcoming = upcoming;
    }

    public List<RecentGradeDTO> getRecentGrades() {
        return recentGrades;
    }

    public void setRecentGrades(List<RecentGradeDTO> recentGrades) {
        this.recentGrades = recentGrades;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(int submittedCount) {
        this.submittedCount = submittedCount;
    }
}
