package model;

import java.time.LocalDateTime;

public class Assignment {

    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_PUBLISHED = "Published";
    public static final String STATUS_CLOSED = "Closed";

    private int assignmentId;
    private int classId;
    private Integer templateId;
    private String title;
    private String instruction;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private String status;

    public Assignment() {
    }

    public Assignment(int assignmentId, int classId, Integer templateId, String title,
            String instruction, LocalDateTime startDate, LocalDateTime dueDate, String status) {
        this.assignmentId = assignmentId;
        this.classId = classId;
        this.templateId = templateId;
        this.title = title;
        this.instruction = instruction;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}