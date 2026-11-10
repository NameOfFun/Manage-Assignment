package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A graded submission, as shown in the "Recent grades" table of the Student Dashboard.
 */
public class RecentGradeDTO {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int FEEDBACK_PREVIEW_LENGTH = 120;

    private String assignmentTitle;
    private String classCode;
    private double score;
    private String feedback;
    private LocalDateTime evaluatedAt;

    public RecentGradeDTO() {
    }

    public RecentGradeDTO(String assignmentTitle, String classCode, double score,
            String feedback, LocalDateTime evaluatedAt) {
        this.assignmentTitle = assignmentTitle;
        this.classCode = classCode;
        this.score = score;
        this.feedback = feedback;
        this.evaluatedAt = evaluatedAt;
    }

    /** "8.5" */
    public String getScoreText() {
        return String.format(Locale.US, "%.1f", score);
    }

    /** First 120 characters of the feedback, or empty when the teacher left none. */
    public String getFeedbackPreview() {
        if (feedback == null || feedback.isBlank()) {
            return "";
        }
        String text = feedback.strip();
        return text.length() <= FEEDBACK_PREVIEW_LENGTH
                ? text
                : text.substring(0, FEEDBACK_PREVIEW_LENGTH).strip() + "…";
    }

    public String getEvaluatedText() {
        return evaluatedAt == null ? "" : evaluatedAt.format(DATE_FORMAT);
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public String getClassCode() {
        return classCode;
    }

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }
}
