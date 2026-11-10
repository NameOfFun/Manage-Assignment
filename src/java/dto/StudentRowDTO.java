package dto;

import java.util.Collections;
import java.util.List;

public class StudentRowDTO {

    private final int userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final List<String> classCodes;

    public StudentRowDTO(int userId, String username, String fullName, String email, List<String> classCodes) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.classCodes = classCodes == null ? Collections.emptyList() : classCodes;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDisplayName() {
        return fullName == null || fullName.isBlank() ? username : fullName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getClassCodes() {
        return classCodes;
    }
}
