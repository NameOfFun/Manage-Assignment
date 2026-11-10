package dto;

import utils.SemesterUtil;

public class ClassDTO {

	private int id;
	private int subjectId;
	private int teacherId;
	private String classCode;
	private String semester;
	private int year;

	// Display-only fields (filled by ClassDAO joins)
	private String subjectName;
	private String subjectCode;
	private String teacherName;
	private String teacherUsername;
	private int studentCount;
	private int groupCount;
	private int assignmentCount;

	public ClassDTO() {
	}

	public ClassDTO(int id, String classCode, String subjectName, String semester, int year, String teacherName, int assignmentCount) {
		this.id = id;
		this.classCode = classCode;
		this.subjectName = subjectName;
		this.semester = semester;
		this.year = year;
		this.teacherName = teacherName;
		this.assignmentCount = assignmentCount;
	}

	public ClassDTO(int id, int subjectId, int teacherId, String classCode, String semester, int year) {
		this.id = id;
		this.subjectId = subjectId;
		this.teacherId = teacherId;
		this.classCode = classCode;
		this.semester = semester;
		this.year = year;
	}

	public String getSemesterCode() {
		return SemesterUtil.code(semester, year);
	}

	public String getSubjectLabel() {
		if (subjectCode != null && !subjectCode.isBlank()) {
			return subjectCode;
		}
		return subjectName;
	}

	public String getTeacherLabel() {
		if (teacherName != null && !teacherName.isBlank()) {
			return teacherName;
		}
		return teacherUsername;
	}

	public boolean isRemovable() {
		return groupCount == 0 && assignmentCount == 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public int getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(int teacherId) {
		this.teacherId = teacherId;
	}

	public String getClassCode() {
		return classCode;
	}

	public void setClassCode(String classCode) {
		this.classCode = classCode;
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

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public String getTeacherUsername() {
		return teacherUsername;
	}

	public void setTeacherUsername(String teacherUsername) {
		this.teacherUsername = teacherUsername;
	}

	public int getStudentCount() {
		return studentCount;
	}

	public void setStudentCount(int studentCount) {
		this.studentCount = studentCount;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public void setGroupCount(int groupCount) {
		this.groupCount = groupCount;
	}

	public int getAssignmentCount() {
		return assignmentCount;
	}

	public void setAssignmentCount(int assignmentCount) {
		this.assignmentCount = assignmentCount;
	}
}
