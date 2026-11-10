package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ClassStudentDTO {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final LocalDateTime enrollDate;

    public ClassStudentDTO(int userId, String username, String fullName, String email, LocalDateTime enrollDate) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.enrollDate = enrollDate;
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

    public LocalDateTime getEnrollDate() {
        return enrollDate;
    }

    public String getEnrollDateText() {
        return enrollDate == null ? "" : enrollDate.format(DATE);
    }
}
