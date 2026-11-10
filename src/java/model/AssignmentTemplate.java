package model;

public class AssignmentTemplate {

    private int id;
    private int expertId;
    private int subjectId;
    private String title;
    private String instruction;

    public AssignmentTemplate() {}

    public AssignmentTemplate(int id, int expertId, int subjectId, String title, String instruction) {
        this.id = id;
        this.expertId = expertId;
        this.subjectId = subjectId;
        this.title = title;
        this.instruction = instruction;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExpertId() { return expertId; }
    public void setExpertId(int expertId) { this.expertId = expertId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
