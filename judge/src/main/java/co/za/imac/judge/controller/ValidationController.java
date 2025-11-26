package co.za.imac.judge.controller;

import co.za.imac.judge.dto.ValidationResult;
import co.za.imac.judge.service.PilotService;
import co.za.imac.judge.service.ScheduleService;
import co.za.imac.judge.service.SequenceService;
import co.za.imac.judge.service.SequenceValidationService;
import co.za.imac.judge.service.SettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller for sequence validation flow.
 * Validates sequence configurations after sync and before judging can begin.
 */
@Controller
public class ValidationController {

    private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);

    @Autowired
    private SequenceValidationService validationService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private PilotService pilotService;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * Called after sync completes. Validates all sequences and redirects appropriately.
     * If issues found, shows the error screen.
     * If clean, redirects to normal flow.
     */
    @GetMapping("/validate-sequences")
    public String validateSequences(Model model) throws IOException {
        logger.info("Running sequence validation...");

        ValidationResult result = validationService.validateAll();

        model.addAttribute("settings", settingService.getSettings());
        model.addAttribute("errors", result.getErrors());
        model.addAttribute("warnings", result.getWarnings());
        model.addAttribute("hasErrors", result.hasErrors());
        model.addAttribute("hasWarnings", result.hasWarnings());
        model.addAttribute("totalIssues", result.getTotalIssueCount());
        model.addAttribute("isSuccess", !result.hasErrors() && !result.hasWarnings());

        if (!result.hasErrors() && !result.hasWarnings()) {
            logger.info("Validation passed - showing success page");
        }

        // Always show validation page - user clicks to continue
        return "sequence_validation_error";
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
     * Performs sync directly and re-validates (does NOT go to admin page).
     */
    @PostMapping("/validate-sequences/resync")
    public String resync(RedirectAttributes redirectAttributes) {
        logger.info("User requested re-sync from validation error screen");
        try {
            // Perform the sync directly
            pilotService.getPilotsFileFromScore();
            sequenceService.getSequenceFileFromScore();

            // IMPORTANT: Force reload the schedules cache with new data!
            // Without this, validation would use stale cached data
            scheduleService.populateSequences();

            logger.info("Re-sync completed successfully - schedules reloaded");
        } catch (Exception e) {
            logger.error("Re-sync failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("syncError", "Sync failed: " + e.getMessage());
        }
        // Redirect back to validation to show new results
        return "redirect:/validate-sequences";
    }
}
