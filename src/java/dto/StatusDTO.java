package dto;

/**
 * Nhan hien thi dung chung cho status / priority.
 */
public final class StatusDTO {

    private StatusDTO() {
    }

    public static String status(String status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case "TODO":
                return "Chưa làm";
            case "IN_PROGRESS":
                return "Đang làm";
            case "DONE":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã huỷ";
            default:
                return status;
        }
    }

    /**
     * Ten class CSS tuong ung, dung cho badge tren giao dien.
     */
    public static String statusClass(String status) {
        if (status == null) {
            return "badge";
        }
        switch (status) {
            case "TODO":
                return "badge badge-todo";
            case "IN_PROGRESS":
                return "badge badge-doing";
            case "DONE":
                return "badge badge-done";
            case "CANCELLED":
                return "badge badge-cancel";
            default:
                return "badge";
        }
    }

    public static String priority(String priority) {
        if (priority == null) {
            return "-";
        }
        switch (priority) {
            case "LOW":
                return "Thấp";
            case "MEDIUM":
                return "Trung bình";
            case "HIGH":
                return "Cao";
            default:
                return priority;
        }
    }

    public static String priorityClass(String priority) {
        if (priority == null) {
            return "prio";
        }
        switch (priority) {
            case "LOW":
                return "prio prio-low";
            case "MEDIUM":
                return "prio prio-medium";
            case "HIGH":
                return "prio prio-high";
            default:
                return "prio";
        }
    }
}
