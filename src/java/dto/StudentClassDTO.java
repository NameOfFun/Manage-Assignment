package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import utils.SemesterUtil;

public class StudentClassDTO {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int classId;
    private final String classCode;
    private final String subjectName;
    private final String subjectCode;
    private final String semester;
    private final int year;
    private final LocalDateTime enrollDate;
    private final int groupId;

    public StudentClassDTO(int classId, String classCode, String subjectName, String subjectCode,
            String semester, int year, LocalDateTime enrollDate, int groupId) {
        this.classId = classId;
        this.classCode = classCode;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.semester = semester;
        this.year = year;
        this.enrollDate = enrollDate;
        this.groupId = groupId;
    }

    public int getClassId() {
        return classId;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getSubjectLabel() {
        return subjectCode != null && !subjectCode.isBlank() ? subjectCode : subjectName;
    }

    public String getSemesterCode() {
        return SemesterUtil.code(semester, year);
    }

    public String getEnrollDateText() {
        return enrollDate == null ? "" : enrollDate.format(DATE);
    }

    public int getGroupId() {
        return groupId;
    }
}
