package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One submission of a group for an assignment. Also used as a row of the teacher's
 * submission list, where a group that has not submitted yet has submissionId = 0.
 */
public class SubmissionDTO {

    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_GRADED = "Graded";
    /** file_url prefix for files uploaded to the server; anything else is an external link. */
    public static final String UPLOAD_PREFIX = "upload:";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int submissionId;
    private int assignmentId;
    private int studentId;
    private String studentName;
    private int groupId;
    private String groupMembers;
    private LocalDateTime submitTime;
    private LocalDateTime dueDate;
    private String status;
    private Double score;
    private String feedback;
    private LocalDateTime evaluatedAt;
    private String fileUrl;

    // Assignment / class context, filled by the Submission List and Submission Details queries
    private String assignmentTitle;
    private String assignmentStatus;
    private int classId;
    private String classCode;
    private String subjectName;
    private String semester;
    private int year;
    private int classTeacherId;
    private String studentUsername;
    private String graderName;

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getClassTeacherId() {
        return classTeacherId;
    }

    public void setClassTeacherId(int classTeacherId) {
        this.classTeacherId = classTeacherId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getGraderName() {
        return graderName;
    }

    public void setGraderName(String graderName) {
        this.graderName = graderName;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public String getDueDateText() {
        return dueDate == null ? "-" : dueDate.format(DATE_FORMAT);
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public boolean isSubmitted() {
        return submissionId > 0;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        this.groupMembers = groupMembers;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public String getSubmitTimeText() {
        return submitTime == null ? "-" : submitTime.format(DATE_FORMAT);
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    /** Submitted after the deadline (e.g. the teacher moved the due date earlier). */
    public boolean isLate() {
        return submitTime != null && dueDate != null && submitTime.isAfter(dueDate);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isGraded() {
        return score != null;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getScoreText() {
        if (score == null) {
            return "-";
        }
        return score == Math.floor(score) ? String.valueOf(score.intValue()) : String.valueOf(score);
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public String getEvaluatedText() {
        return evaluatedAt == null ? "-" : evaluatedAt.format(DATE_FORMAT);
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public boolean isUploaded() {
        return fileUrl != null && fileUrl.startsWith(UPLOAD_PREFIX);
    }

    /** Name of the stored file on disk, or null for a link. */
    public String getStoredFileName() {
        return isUploaded() ? fileUrl.substring(UPLOAD_PREFIX.length()) : null;
    }

    /** Original file name shown to users (the stored name minus the "a1_g2_123456_" prefix). */
    public String getFileName() {
        String stored = getStoredFileName();
        if (stored == null) {
            return fileUrl;
        }
        String[] parts = stored.split("_", 4);
        return parts.length == 4 ? parts[3] : stored;
    }
}
