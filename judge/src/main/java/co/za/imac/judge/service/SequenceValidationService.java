package co.za.imac.judge.service;

import co.za.imac.judge.dto.Pilot;
import co.za.imac.judge.dto.ScheduleDTO;
import co.za.imac.judge.dto.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pre-flight validation service for sequence configuration.
 * Validates that all sequence configurations are correct after sync,
 * before judging can begin.
 */
@Service
public class SequenceValidationService {

    private static final Logger logger = LoggerFactory.getLogger(SequenceValidationService.class);

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private PilotService pilotService;

    @Autowired
    private SequenceFolderResolver resolver;

    @Autowired
    private CompService compService;

    /**
     * Validates all sequence configurations after sync.
     * @return ValidationResult with errors and warnings
     */
    public ValidationResult validateAll() {
        logger.info("Starting sequence validation...");
        ValidationResult result = new ValidationResult();

        try {
            // Check 1: Pilot classes without sequences
            checkPilotClassCoverage(result);

            // Check 2: Folder resolution for each sequence
            checkFolderResolution(result);

            // Check 3: Round range conflicts
            checkRoundRangeConflicts(result);

            // Check 4: Unknown sequences without short_desc
            checkUnknownSequences(result);

            // Check 5: Round coverage gaps
            checkRoundCoverageGaps(result);

        } catch (Exception e) {
            logger.error("Error during validation", e);
            result.addError("SYSTEM", "Validation failed: " + e.getMessage(),
                    "Please check logs for details");
        }

        logger.info("Validation complete: {} errors, {} warnings",
                result.getErrors().size(), result.getWarnings().size());
        return result;
    }

    /**
     * Check 1: Pilot classes without sequences.
     * Ensures every pilot class has at least one sequence defined.
     */
    private void checkPilotClassCoverage(ValidationResult result) {
        try {
            List<Pilot> pilots = pilotService.getPilots(true);
            if (pilots == null || pilots.isEmpty()) {
                logger.debug("No pilots loaded - skipping pilot class coverage check");
                return;
            }

            // Get unique pilot classes
            Set<String> pilotClasses = pilots.stream()
                    .filter(p -> p.getClassString() != null)
                    .map(p -> p.getClassString().toUpperCase())
                    .collect(Collectors.toSet());

            // Get classes from schedules
            Map<Integer, ScheduleDTO> schedules = scheduleService.getSchedules();
            if (schedules == null || schedules.isEmpty()) {
                result.addError("SEQUENCES", "No sequences loaded",
                        "The sequences.dat file may be missing or empty");
                return;
            }

            Set<String> sequenceClasses = schedules.values().stream()
                    .filter(s -> s.getComp_class() != null)
                    .map(s -> s.getComp_class().toUpperCase())
                    .collect(Collectors.toSet());

            // Check each pilot class has sequences
            for (String pilotClass : pilotClasses) {
                if (!sequenceClasses.contains(pilotClass) &&
                    !"FREESTYLE".equalsIgnoreCase(pilotClass)) {

                    List<String> affectedPilots = pilots.stream()
                            .filter(p -> p.getClassString() != null &&
                                    p.getClassString().equalsIgnoreCase(pilotClass))
                            .map(Pilot::getName)
                            .collect(Collectors.toList());

                    result.addError(
                            pilotClass + " class",
                            affectedPilots.size() + " pilot(s) registered but NO SEQUENCES defined",
                            "Affected pilots: " + String.join(", ", affectedPilots)
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error checking pilot class coverage", e);
            result.addWarning("SYSTEM", "Could not check pilot class coverage", e.getMessage());
        }
    }

    /**
     * Check 2: Folder resolution for each sequence.
     * Ensures each sequence's folder can be resolved.
     */
    private void checkFolderResolution(ValidationResult result) {
        Map<Integer, ScheduleDTO> schedules = scheduleService.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        // Get default sequenceType from competition settings
        String defaultSequenceType = "std";
        try {
            var comp = compService.getComp();
            if (comp != null && comp.getSequenceType() != null) {
                defaultSequenceType = comp.getSequenceType();
            }
        } catch (Exception e) {
            logger.debug("Could not get competition settings for sequenceType");
        }

        for (ScheduleDTO schedule : schedules.values()) {
            // Test with min_round
            String resolved = resolver.resolve(
                    schedule.getComp_class(),
                    schedule.getType(),
                    schedule.getMin_round() != null ? schedule.getMin_round() : 1,
                    defaultSequenceType
            );

            if (SequenceFolderResolver.FAIL_FOLDER.equals(resolved)) {
                String shortDesc = schedule.getShort_desc();
                String context = schedule.getComp_class() + " " + schedule.getType();

                if (shortDesc == null || shortDesc.trim().isEmpty()) {
                    result.addError(
                            context,
                            "No short_desc defined - cannot resolve folder",
                            "Rounds " + schedule.getMin_round() + "-" + schedule.getMax_round()
                    );
                } else {
                    result.addError(
                            context,
                            "Folder not found: " + shortDesc,
                            "Rounds " + schedule.getMin_round() + "-" + schedule.getMax_round()
                    );
                }
            }
        }
    }

    /**
     * Check 3: Duplicate and overlapping round range conflicts.
     * Within same class+type:
     * - Same short_desc = WARNING (redundant but harmless)
     * - Different/no short_desc = ERROR (ambiguous, will use FAIL folder)
     */
    private void checkRoundRangeConflicts(ValidationResult result) {
        Map<String, List<ScheduleDTO>> byClassType = groupByClassType();

        for (Map.Entry<String, List<ScheduleDTO>> entry : byClassType.entrySet()) {
            String key = entry.getKey();
            List<ScheduleDTO> schedules = entry.getValue();

            if (schedules.size() < 2) {
                continue; // No conflicts possible with single entry
            }

            for (int i = 0; i < schedules.size(); i++) {
                for (int j = i + 1; j < schedules.size(); j++) {
                    ScheduleDTO a = schedules.get(i);
                    ScheduleDTO b = schedules.get(j);

                    // Check for duplicate min_round OR overlapping ranges
                    boolean isDuplicate = a.getMin_round() != null && b.getMin_round() != null &&
                            a.getMin_round().equals(b.getMin_round());
                    boolean isOverlapping = rangesOverlap(a, b);

                    if (isDuplicate || isOverlapping) {
                        String aDesc = a.getShort_desc() != null && !a.getShort_desc().trim().isEmpty()
                                ? a.getShort_desc() : null;
                        String bDesc = b.getShort_desc() != null && !b.getShort_desc().trim().isEmpty()
                                ? b.getShort_desc() : null;

                        boolean sameShortDesc = aDesc != null && bDesc != null && aDesc.equalsIgnoreCase(bDesc);

                        String conflictType = isDuplicate ? "Duplicate" : "Overlapping";
                        String roundInfo = isDuplicate
                                ? "min_round=" + a.getMin_round()
                                : "rounds " + a.getMin_round() + "-" + a.getMax_round() +
                                  " and " + b.getMin_round() + "-" + b.getMax_round();

                        if (sameShortDesc) {
                            // Same short_desc = WARNING (redundant but works)
                            result.addWarning(
                                    key,
                                    conflictType + " sequences identified (" + roundInfo + ")",
                                    "Both use: " + aDesc
                            );
                        } else {
                            // Different or missing short_desc = ERROR (ambiguous)
                            String foundDesc = (aDesc != null ? aDesc : "(blank)") +
                                    " and " + (bDesc != null ? bDesc : "(blank)");
                            result.addError(
                                    key,
                                    conflictType + " sequences - unable to resolve (" + roundInfo + ")",
                                    "Found: " + foundDesc + " - Using FAIL folder"
                            );
                        }
                    }
                }
            }
        }
    }

    /**
     * Check 4: Unknown sequences without short_desc.
     * UNKNOWN sequences cannot be derived and must have short_desc.
     */
    private void checkUnknownSequences(ValidationResult result) {
        Map<Integer, ScheduleDTO> schedules = scheduleService.getSchedules();
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        for (ScheduleDTO schedule : schedules.values()) {
            if ("UNKNOWN".equalsIgnoreCase(schedule.getType())) {
                String shortDesc = schedule.getShort_desc();
                if (shortDesc == null || shortDesc.trim().isEmpty()) {
                    result.addError(
                            schedule.getComp_class() + " UNKNOWN",
                            "No short_desc - cannot resolve folder",
                            "Rounds " + schedule.getMin_round() + "-" + schedule.getMax_round() +
                                    " will use FAIL folder if not fixed"
                    );
                }
            }
        }
    }


    /**
     * Check 5: Round coverage gaps.
     * Warns when there are gaps in round coverage (e.g., 1-3 and 5-6 defined, round 4 uncovered).
     */
    private void checkRoundCoverageGaps(ValidationResult result) {
        Map<String, List<ScheduleDTO>> byClassType = groupByClassType();

        for (Map.Entry<String, List<ScheduleDTO>> entry : byClassType.entrySet()) {
            String key = entry.getKey();
            List<ScheduleDTO> schedules = entry.getValue();

            if (schedules.isEmpty()) continue;

            // Sort by min_round
            schedules.sort((a, b) -> {
                int aMin = a.getMin_round() != null ? a.getMin_round() : 0;
                int bMin = b.getMin_round() != null ? b.getMin_round() : 0;
                return Integer.compare(aMin, bMin);
            });

            // Check for gaps between consecutive ranges
            for (int i = 0; i < schedules.size() - 1; i++) {
                ScheduleDTO current = schedules.get(i);
                ScheduleDTO next = schedules.get(i + 1);

                int currentMax = current.getMax_round() != null ? current.getMax_round() : 0;
                int nextMin = next.getMin_round() != null ? next.getMin_round() : 0;

                // Gap exists if next min_round > current max_round + 1
                if (nextMin > currentMax + 1) {
                    int gapStart = currentMax + 1;
                    int gapEnd = nextMin - 1;
                    String gapRange = gapStart == gapEnd ? "round " + gapStart : "rounds " + gapStart + "-" + gapEnd;
                    result.addWarning(
                            key,
                            "Round coverage gap: " + gapRange + " undefined",
                            "Will use FAIL folder for undefined rounds"
                    );
                }
            }
        }
    }

    /**
     * Group schedules by CLASS:TYPE key.
     */
    private Map<String, List<ScheduleDTO>> groupByClassType() {
        Map<String, List<ScheduleDTO>> result = new HashMap<>();

        Map<Integer, ScheduleDTO> schedules = scheduleService.getSchedules();
        if (schedules == null) {
            return result;
        }

        for (ScheduleDTO schedule : schedules.values()) {
            String key;
            if ("FREESTYLE".equalsIgnoreCase(schedule.getType())) {
                key = "FREESTYLE";
            } else {
                key = (schedule.getComp_class() != null ? schedule.getComp_class().toUpperCase() : "UNKNOWN")
                        + ":" + (schedule.getType() != null ? schedule.getType().toUpperCase() : "UNKNOWN");
            }

            result.computeIfAbsent(key, k -> new ArrayList<>()).add(schedule);
        }

        return result;
    }

    /**
     * Check if two schedule round ranges overlap.
     */
    private boolean rangesOverlap(ScheduleDTO a, ScheduleDTO b) {
        Integer aMin = a.getMin_round() != null ? a.getMin_round() : 0;
        Integer aMax = a.getMax_round() != null ? a.getMax_round() : Integer.MAX_VALUE;
        Integer bMin = b.getMin_round() != null ? b.getMin_round() : 0;
        Integer bMax = b.getMax_round() != null ? b.getMax_round() : Integer.MAX_VALUE;

        return aMin <= bMax && bMin <= aMax;
    }
}
