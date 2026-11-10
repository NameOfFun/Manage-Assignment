package dto;

import java.util.LinkedHashMap;
import java.util.Map;


public class ClassResultDTO {

    private boolean success;
    private String message;
    private final Map<String, String> errors = new LinkedHashMap<>();
    private ClassDTO classDTO;

    public static ClassResultDTO ok(String message, ClassDTO classDTO) {
        ClassResultDTO r = new ClassResultDTO();
        r.success = true;
        r.message = message;
        r.classDTO = classDTO;
        return r;
    }

    public static ClassResultDTO fail(String message) {
        ClassResultDTO r = new ClassResultDTO();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void addError(String field, String message) {
        errors.put(field, message);
        success = false;
    }

    public ClassDTO getClassDTO() {
        return classDTO;
    }
}
