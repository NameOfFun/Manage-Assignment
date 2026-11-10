package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentDTO {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	private int assignmentId;
	private int classId;
	private String title;
	private String instruction;
	private LocalDateTime startDate;
	private LocalDateTime dueDate;
	private String status;
	private String classCode;
	private String subjectName;
	private int pendingSubmissionCount;
	private String semester;
	private int year;
	private boolean submitted;

	public AssignmentDTO() {
	}

	public AssignmentDTO(int assignmentId, String title, String classCode, LocalDateTime dueDate, boolean submitted) {
		this.assignmentId = assignmentId;
		this.title = title;
		this.classCode = classCode;
		this.dueDate = dueDate;
		this.submitted = submitted;
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

	public String getStartDateText() {
		return startDate == null ? "-" : startDate.format(DATE_FORMAT);
	}

	public String getStartDateInput() {
		return startDate == null ? "" : startDate.format(INPUT_FORMAT);
	}

	public LocalDateTime getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDateTime dueDate) {
		this.dueDate = dueDate;
	}

	public String getDueDateText() {
		return dueDate == null ? "-" : dueDate.format(DATE_FORMAT);
	}

	public String getDueDateInput() {
		return dueDate == null ? "" : dueDate.format(INPUT_FORMAT);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public int getPendingSubmissionCount() {
		return pendingSubmissionCount;
	}

	public void setPendingSubmissionCount(int pendingSubmissionCount) {
		this.pendingSubmissionCount = pendingSubmissionCount;
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

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

}
