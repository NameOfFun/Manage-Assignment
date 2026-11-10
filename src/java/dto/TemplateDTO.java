package dto;

import model.AssignmentTemplate;

public class TemplateDTO {

    private boolean success;
    private String message;
    private AssignmentTemplate template;

    public TemplateDTO(boolean success, String message, AssignmentTemplate template) {
        this.success = success;
        this.message = message;
        this.template = template;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public AssignmentTemplate getTemplate() { return template; }
}
