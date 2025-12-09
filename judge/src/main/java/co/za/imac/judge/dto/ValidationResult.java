package co.za.imac.judge.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of sequence configuration validation.
 * Holds separate lists for errors (must fix) and warnings (review recommended).
 */
public class ValidationResult {

    private List<ValidationIssue> errors = new ArrayList<>();
    private List<ValidationIssue> warnings = new ArrayList<>();

    public ValidationResult() {
    }

    /**
     * Check if there are any errors.
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there are any warnings.
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Check if validation passed with no issues.
     * @return true if no errors and no warnings
     */
    public boolean isClean() {
        return errors.isEmpty() && warnings.isEmpty();
    }

    /**
     * Add an error to the result.
     * @param context The context (e.g., "SPORTSMAN KNOWN")
     * @param message The error message
     * @param detail Additional details
     */
    public void addError(String context, String message, String detail) {
        errors.add(new ValidationIssue(context, message, detail));
    }

    /**
     * Add a warning to the result.
     * @param context The context (e.g., "SPORTSMAN KNOWN")
     * @param message The warning message
     * @param detail Additional details
     */
    public void addWarning(String context, String message, String detail) {
        warnings.add(new ValidationIssue(context, message, detail));
    }

    public List<ValidationIssue> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationIssue> errors) {
        this.errors = errors;
    }

    public List<ValidationIssue> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<ValidationIssue> warnings) {
        this.warnings = warnings;
    }

    /**
     * Get total count of all issues (errors + warnings).
     * @return total issue count
     */
    public int getTotalIssueCount() {
        return errors.size() + warnings.size();
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                '}';
    }
}
