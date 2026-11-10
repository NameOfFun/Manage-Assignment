package dto;

import utils.SemesterUtil;


public class SemesterOptionDTO {

    private final String semester;
    private final int year;

    public SemesterOptionDTO(String semester, int year) {
        this.semester = semester;
        this.year = year;
    }

    
    public String getValue() {
        return semester + "-" + year;
    }

    
    public String getLabel() {
        return SemesterUtil.code(semester, year);
    }

    public String getSemester() {
        return semester;
    }

    public int getYear() {
        return year;
    }
}
