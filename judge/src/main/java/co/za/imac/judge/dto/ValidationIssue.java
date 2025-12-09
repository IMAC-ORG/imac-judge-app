package co.za.imac.judge.dto;

/**
 * Represents a single validation issue (error or warning) found during
 * sequence configuration validation.
 */
public class ValidationIssue {

    private String context;      // e.g., "SPORTSMAN KNOWN"
    private String message;      // e.g., "Folder not found: SPK_99X"
    private String detail;       // e.g., "Affected pilots: John, Jane"

    public ValidationIssue() {
    }

    public ValidationIssue(String context, String message, String detail) {
        this.context = context;
        this.message = message;
        this.detail = detail;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ValidationIssue{" +
                "context='" + context + '\'' +
                ", message='" + message + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
