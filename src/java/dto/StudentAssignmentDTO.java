package dto;

import java.time.LocalDateTime;
import model.Assignment;

/** One assignment as a student sees it: the assignment, the student's group and the group's submission. */
public class StudentAssignmentDTO {

    private AssignmentDTO assignment;
    private int groupId; // 0 when the student has no group in this class
    private SubmissionDTO submission; // null when the group has not submitted yet

    public AssignmentDTO getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentDTO assignment) {
        this.assignment = assignment;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public SubmissionDTO getSubmission() {
        return submission;
    }

    public void setSubmission(SubmissionDTO submission) {
        this.submission = submission;
    }

    public boolean isSubmitted() {
        return submission != null;
    }

    public boolean isGraded() {
        return submission != null && submission.isGraded();
    }

    public boolean isClosed() {
        return Assignment.STATUS_CLOSED.equalsIgnoreCase(assignment.getStatus());
    }

    public boolean isPastDue() {
        return assignment.getDueDate().isBefore(LocalDateTime.now());
    }

    /** Why the group cannot submit now, or null if it can. */
    public String getBlockedReason() {
        if (groupId == 0) {
            return "You are not in a group of this class yet. Please contact your teacher.";
        }
        if (isClosed()) {
            return "This assignment is closed and no longer accepts submissions.";
        }
        if (isGraded()) {
            return "This submission has already been graded and cannot be changed.";
        }
        return null;
    }

    public boolean isCanSubmit() {
        return getBlockedReason() == null;
    }

    public String getStatusText() {
        if (isGraded()) {
            return "Graded";
        }
        if (isSubmitted()) {
            return submission.isLate() ? "Submitted late" : "Submitted";
        }
        if (isClosed()) {
            return "Closed";
        }
        return isPastDue() ? "Overdue" : "Not submitted";
    }

    /** CSS class of the status badge: ok (green), bad (red) or empty (neutral). */
    public String getStatusClass() {
        if (isGraded()) {
            return "ok";
        }
        if (isSubmitted()) {
            return submission.isLate() ? "bad" : "ok";
        }
        return isClosed() || isPastDue() ? "bad" : "";
    }
}
