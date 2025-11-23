package co.za.imac.judge.controller;

import co.za.imac.judge.dto.ValidationResult;
import co.za.imac.judge.service.SequenceValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for sequence validation flow.
 * Validates sequence configurations after sync and before judging can begin.
 */
@Controller
public class ValidationController {

    private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);

    @Autowired
    private SequenceValidationService validationService;

    /**
     * Called after sync completes. Validates all sequences and redirects appropriately.
     * If issues found, shows the error screen.
     * If clean, redirects to normal flow.
     */
    @GetMapping("/validate-sequences")
    public String validateSequences(Model model) {
        logger.info("Running sequence validation...");

        ValidationResult result = validationService.validateAll();

        if (result.hasErrors() || result.hasWarnings()) {
            model.addAttribute("errors", result.getErrors());
            model.addAttribute("warnings", result.getWarnings());
            model.addAttribute("hasErrors", result.hasErrors());
            model.addAttribute("hasWarnings", result.hasWarnings());
            model.addAttribute("totalIssues", result.getTotalIssueCount());
            return "sequence_validation_error";
        }

        // All good - redirect to normal flow
        logger.info("Validation passed - redirecting to pilot list");
        return "redirect:/pilot-list-global";
    }

    /**
     * User chose to continue despite validation errors.
     * Sets a session flag and redirects to normal flow.
     */
    @PostMapping("/validate-sequences/continue")
    public String continueAnyway(HttpSession session) {
        logger.warn("User chose to continue despite validation errors");
        session.setAttribute("validationOverridden", true);
        return "redirect:/pilot-list-global";
    }

    /**
     * User requested to re-sync from the validation error screen.
     * Redirects to the sync/newcomp page.
     */
    @PostMapping("/validate-sequences/resync")
    public String resync() {
        logger.info("User requested re-sync from validation error screen");
        return "redirect:/newcomp";
    }
}
