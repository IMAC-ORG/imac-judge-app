package co.za.imac.judge.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import co.za.imac.judge.dto.*;
import co.za.imac.judge.service.RoundsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import co.za.imac.judge.utils.SettingUtils;

import co.za.imac.judge.service.CompService;
import co.za.imac.judge.service.InfoCollectorService;
import co.za.imac.judge.service.PilotService;
import co.za.imac.judge.service.ScheduleService;
import co.za.imac.judge.service.SequenceService;
import co.za.imac.judge.service.SettingService;

@RestController
public class APIController {
    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    private CompService compService;
    @Autowired
    private RoundsService roundsService;
    @Autowired
    private PilotService pilotService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private InfoCollectorService infoCollectorService;
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/api/comp")
    public CompDTO getComp() throws IOException, ParserConfigurationException, SAXException {
        return compService.getComp();
    }

    /**
     * Update local comp settings without contacting Score server.
     * Accepts: sequences (int), sequenceType (String), score_mode (String)
     */
    @PostMapping("/api/comp/local")
    public ResponseEntity<String> updateLocalCompSettings(@RequestBody CompDTO comp) throws IOException {
        Map<String, Object> result = new HashMap<>();

        CompDTO currentComp = compService.getComp();
        if (currentComp == null) {
            result.put("result", "fail");
            result.put("message", "No competition loaded. Load an event first.");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }

        // Update only the local settings that were provided
        if (comp.getSequences() > 0) {
            currentComp.setSequences(comp.getSequences());
            logger.info("Updated sequences to: {}", comp.getSequences());
        }
        if (comp.getSequenceType() != null) {
            currentComp.setSequenceType(comp.getSequenceType());
            logger.info("Updated sequenceType to: {}", comp.getSequenceType());
        }
        if (comp.getScore_mode() != null) {
            currentComp.setScore_mode(comp.getScore_mode());
            logger.info("Updated score_mode to: {}", comp.getScore_mode());
        }

        // Save locally without Score contact
        if (compService.saveCompToFileLocal()) {
            result.put("result", "ok");
            result.put("message", "Local settings updated.");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
        } else {
            result.put("result", "fail");
            result.put("message", "Could not save settings.");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/comp")
    public ResponseEntity<String> createComp(@RequestBody CompDTO comp,
            @RequestParam(name = "edit", required = true) Boolean editComp)
            throws IOException, ParserConfigurationException, SAXException, URISyntaxException {

        Map<String, Object> result = new HashMap<>();
        
        logger.debug("Comp data received:");
        logger.debug(new Gson().toJson(comp));
       
        // fetch pilots
        pilotService.getPilotsFileFromScore(); // Reloading here means we can add pilots mid comp. But if their id/name
        compService.enrichCompWithCompInfoFromScore(comp); // Add the names and ID.
       
       
        if (!editComp) {
        //archive exising pilots and scores on new comp creation
            settingService.backupAllFiles();
            pilotService.setupPilotScores();
        }

        // fetch seqs
        sequenceService.getSequenceFileFromScore();
        
        CompDTO newComp = compService.createCompFromRequest(comp);
        if (newComp == null) {
            result.put("result", "fail");
            if (editComp)
                result.put("message", "Could not edit comp.");
            else
                result.put("message", "Could not create new comp.");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);

        } else {
            result.put("result", "ok");
            if (editComp) {
                result.put("action", "edit");
                result.put("message", "Comp " + newComp.getComp_name() + " is edited.");
            } else {
                result.put("action", "create");
                result.put("message", "New comp " + newComp.getComp_name() + " is created.");
            }

            result.put("comp", new Gson().toJson(newComp));

            //now try to fetch unknown figures if there are UNKNOWN sequences
            try {
                if (sequenceService.hasUnknownSequences()) {
                    sequenceService.getUnknownFiguresZip();
                } else {
                    logger.info("No UNKNOWN sequences found, skipping unknown figures download");
                }
            } catch (Exception e) {
                logger.warn("Failed to download unknown figures: {}", e.getMessage());
                // Continue with comp creation even if unknown figures download fails
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Location", "/api/comp");

            return ResponseEntity.created(new URI("/api/comp")).body(new Gson().toJson(result));
        }
    }

    @PostMapping("/api/rounds")
    public ResponseEntity<String> createRound(@RequestBody RoundDTO round,
            @RequestParam(name = "edit", required = false, defaultValue = "false") Boolean editRound)
            throws IOException, ParserConfigurationException, SAXException {

        Map<String, Object> result = new HashMap<>();

        logger.debug("Creating new round:");
        logger.debug(new Gson().toJson(round));

        if (editRound) {
            // Do edit...
            result.put("result", "fail");
            result.put("message", "editing of rounds is not yet implemented.");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.NOT_IMPLEMENTED);
        } else {
            // Before we can add a new round, we need to get round number / round ID
            // editRound is false so we're just going to overwrite what we were given.

            RoundDTO newRound = roundsService.addRound(round);
            if (newRound != null) {
                roundsService.saveRoundsToFile();
                result.put("result", "ok");
                result.put("message", "New round created.");
                result.put("new_round", new Gson().toJson(newRound));

                // Set it active if we are in byRound mode.
                if ("byRound".equalsIgnoreCase(compService.getComp().getScore_mode())) {
                    roundsService.activateRoundForScoring(newRound.getRound_id());
                }

                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
            } else
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/rounds/phase")
    public ResponseEntity<String> adjustRound(@RequestBody Map<String, Object> payload)
            throws IOException, ParserConfigurationException, SAXException {
        logger.debug("Performing action " + payload.get("action") + " on round " + payload.get("round_id") + ".");
        Map<String, Object> result = new HashMap<>();
        result.put("action", payload.get("action"));
        result.put("message", "");

        Map<String, Object> res;
        switch ((String) payload.get("action")) {
            case "fly":
                res = roundsService.activateRoundForScoring(Integer.parseInt((String) payload.get("round_id")));
                if ((Boolean) res.get("success")) {
                    result.put("result", "ok");
                } else {
                    result.put("result", "fail");
                    result.put("message", res.get("message"));
                    return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
                }
                break;
            case "close":
                res = roundsService.closeRound(Integer.parseInt((String) payload.get("round_id")));
                if ((Boolean) res.get("success")) {
                    result.put("result", "ok");
                } else {
                    result.put("result", "fail");
                    result.put("message", res.get("message"));
                    return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
                }
                break;
            default:
                result.put("result", "fail");
                result.put("message", "Unknown action.");
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
    }

    @GetMapping("/api/pilots/sync")
    public String syncPilots() throws IOException, ParserConfigurationException, SAXException {
        // fetch pilots
        pilotService.getPilotsFileFromScore();
        sequenceService.getSequenceFileFromScore();
        // Force reload ScheduleService cache with new sequence data
        scheduleService.populateSequences();
        // pilotService.setupPilotScores();
        Map<String, Object> result = new HashMap<>();
        result.put("sync", "ok");
        return new Gson().toJson(result);
    }

    @GetMapping("/api/scores/sync")
    public String syncScores() throws Exception {
        // fetch pilots
        pilotService.syncPilotsToScoreWebService();
        Map<String, Object> result = new HashMap<>();
        result.put("sync", "ok");
        return new Gson().toJson(result);
    }

    @PostMapping("/api/score")
    public PilotScores submitScores(@RequestBody PilotScoreDTO pilotScoreDTO)
            throws ParserConfigurationException, SAXException, IOException {
        System.out.println(new Gson().toJson(pilotScoreDTO));
        return pilotService.submitScore(pilotScoreDTO);
    }

    @GetMapping("/api/settings")
    public SettingDTO getSettings() throws IOException, ParserConfigurationException, SAXException {
        return settingService.getSettings();
    }
    
    @PostMapping("/api/settings")
    public ResponseEntity<String> updateSettings(@RequestBody SettingDTO setting)
            throws IOException, ParserConfigurationException, SAXException {
        Map<String, Object> result = new HashMap<>();
        try {
            settingService.updateSettings(setting);
            result.put("result", "ok");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
        } catch (ConnectException e) {
            logger.error("Could not update settings. " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not update settings.");
        }
    }

    @GetMapping("/api/version")
    public Map<String, String> getVersion() {
        Map<String, String> result = new HashMap<>();
        result.put("appVersion", co.za.imac.judge.JudgeApplication.getAppVersion());
        return result;
    }

    @GetMapping("/api/getinfo")
    public InfoJson getLatestInfo() {
        InfoJson info = infoCollectorService.collectInfo();
        return info;
    }

    /**
     * Lightweight battery percentage endpoint.
     * Much faster than /api/getinfo - only reads I2C sensor.
     */
    @GetMapping("/api/battery")
    public Map<String, Integer> getBatteryPercent() {
        Map<String, Integer> result = new HashMap<>();
        result.put("percent", infoCollectorService.getBatteryPercent());
        return result;
    }

    @PostMapping("/api/pilot/{pilotId}/advance-round")
    public ResponseEntity<PilotScores> advanceRound(
            @PathVariable String pilotId,
            @RequestParam(name = "type", required = true) String roundType)
            throws IOException, ParserConfigurationException, SAXException {

        logger.info("Advancing round for pilot {} type {}", pilotId, roundType);

        // Get pilot
        Pilot pilot = pilotService.getPilot(pilotId);
        if (pilot == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pilot not found");
        }

        // Get pilot scores
        PilotScores pilotScores = pilotService.getPilotScores(pilot);

        // Increment the round for this type and reset sequence to 1
        pilotScores.incrementActiveRound(roundType);
        pilotScores.setActiveSequence(1);

        // Save the updated pilot scores
        pilotService.savePilotScoresToFile(pilotScores);

        logger.info("Advanced pilot {} to round {} sequence 1 for type {}",
                    pilotId, pilotScores.getActiveRound(roundType), roundType);

        return ResponseEntity.ok(pilotScores);
    }

    @GetMapping("/api/scores/mismatches")
    public ResponseEntity<String> getScoreMismatches() throws IOException, ParserConfigurationException, SAXException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> mismatches = new ArrayList<>();

        // Get all pilots and their scores
        List<Pilot> pilots = pilotService.getPilots();

        // Group pilots by class
        Map<String, List<Pilot>> pilotsByClass = new HashMap<>();
        for (Pilot pilot : pilots) {
            String className = pilot.getClassString();
            if (className == null) continue;
            pilotsByClass.computeIfAbsent(className.toUpperCase(), k -> new ArrayList<>()).add(pilot);
        }

        // Check each class for KNOWN and UNKNOWN round types
        String[] classRoundTypes = {"KNOWN", "UNKNOWN"};

        for (String className : pilotsByClass.keySet()) {
            List<Pilot> classPilots = pilotsByClass.get(className);

            for (String roundType : classRoundTypes) {
                checkAndAddMismatches(classPilots, roundType, className, mismatches);
            }
        }

        // Check FREESTYLE separately (cross-class, only runs once)
        List<Pilot> freestylePilots = pilots.stream()
                .filter(p -> Boolean.TRUE.equals(p.getFreestyle()))
                .toList();
        if (!freestylePilots.isEmpty()) {
            checkAndAddMismatches(freestylePilots, "FREESTYLE", "FREESTYLE", mismatches);
        }

        result.put("mismatches", mismatches);
        return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
    }

    /**
     * Compare semantic versions in format #.# or #.#.#
     * @param version1 First version to compare
     * @param version2 Second version to compare
     * @return true if version1 > version2, false otherwise
     */
    private boolean isVersionGreater(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return false;
        }

        // Remove 'v' prefix if present
        version1 = version1.replaceFirst("^v", "");
        version2 = version2.replaceFirst("^v", "");

        // Split versions into parts
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");

        // Compare each part numerically
        for (int i = 0; i < Math.max(v1Parts.length, v2Parts.length); i++) {
            int v1Num = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Num = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1Num > v2Num) {
                return true;
            } else if (v1Num < v2Num) {
                return false;
            }
        }

        // Versions are equal
        return false;
    }

    private int countRoundsForType(PilotScores scores, String roundType) {
        if (scores == null || scores.getScores() == null) return 0;

        Set<Integer> rounds = new HashSet<>();
        for (PScore score : scores.getScores()) {
            if (roundType.equalsIgnoreCase(score.getType())) {
                rounds.add(score.getRound());
            }
        }
        return rounds.size();
    }

    /**
     * Helper method to check for round count mismatches and add to the list if found.
     */
    private void checkAndAddMismatches(List<Pilot> pilots, String roundType, String className,
                                       List<Map<String, Object>> mismatches) throws IOException {
        // Get round counts for each pilot
        Map<Pilot, Integer> roundCounts = new HashMap<>();
        for (Pilot pilot : pilots) {
            PilotScores scores = pilotService.getPilotScores(pilot);
            int count = countRoundsForType(scores, roundType);
            roundCounts.put(pilot, count);
        }

        if (roundCounts.isEmpty()) return;

        // Find expected count using mode (most common value), or median if no clear mode
        Map<Integer, Long> countFrequency = roundCounts.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(c -> c, java.util.stream.Collectors.counting()));

        // Find max frequency
        long maxFreq = countFrequency.values().stream().max(Long::compare).orElse(0L);

        // Count how many values have the max frequency
        long countWithMaxFreq = countFrequency.values().stream().filter(f -> f == maxFreq).count();

        int expectedCount;
        if (countWithMaxFreq == 1) {
            // Clear mode - one value appears more than others
            expectedCount = countFrequency.entrySet().stream()
                    .filter(e -> e.getValue() == maxFreq)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(0);
        } else {
            // No clear mode - use median
            List<Integer> sortedCounts = roundCounts.values().stream()
                    .sorted()
                    .toList();
            int mid = sortedCounts.size() / 2;
            expectedCount = sortedCounts.get(mid);
        }

        // Find pilots that deviate
        List<Map<String, Object>> tooMany = new ArrayList<>();
        List<Map<String, Object>> tooFew = new ArrayList<>();

        for (Map.Entry<Pilot, Integer> entry : roundCounts.entrySet()) {
            Pilot pilot = entry.getKey();
            int count = entry.getValue();

            if (count > expectedCount) {
                Map<String, Object> pilotInfo = new HashMap<>();
                pilotInfo.put("pilotId", pilot.getPrimary_id());
                pilotInfo.put("name", pilot.getName());
                pilotInfo.put("roundCount", count);
                tooMany.add(pilotInfo);
            } else if (count < expectedCount) {
                Map<String, Object> pilotInfo = new HashMap<>();
                pilotInfo.put("pilotId", pilot.getPrimary_id());
                pilotInfo.put("name", pilot.getName());
                pilotInfo.put("roundCount", count);
                tooFew.add(pilotInfo);
            }
        }

        // Only report if there are resolvable mismatches (both source AND destination exist)
        if (!tooMany.isEmpty() && !tooFew.isEmpty()) {
            Map<String, Object> mismatch = new HashMap<>();
            mismatch.put("className", className);
            mismatch.put("roundType", roundType);
            mismatch.put("expectedRounds", expectedCount);
            mismatch.put("tooMany", tooMany);
            mismatch.put("tooFew", tooFew);
            mismatches.add(mismatch);
        }
    }

    @PostMapping("/api/scores/move-round")
    public ResponseEntity<String> moveRound(@RequestBody Map<String, Object> payload)
            throws IOException, ParserConfigurationException, SAXException {
        Map<String, Object> result = new HashMap<>();

        String sourcePilotId = (String) payload.get("sourcePilotId");
        String destPilotId = (String) payload.get("destPilotId");
        String roundType = (String) payload.get("roundType");
        int sourceRound = ((Number) payload.get("sourceRound")).intValue();

        logger.info("Moving {} round {} from pilot {} to pilot {}",
                roundType, sourceRound, sourcePilotId, destPilotId);

        // Get pilots and their scores
        Pilot sourcePilot = pilotService.getPilot(sourcePilotId);
        Pilot destPilot = pilotService.getPilot(destPilotId);

        if (sourcePilot == null || destPilot == null) {
            result.put("result", "fail");
            result.put("message", "Pilot not found");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }

        PilotScores sourceScores = pilotService.getPilotScores(sourcePilot);
        PilotScores destScores = pilotService.getPilotScores(destPilot);

        // Find the scores to move (all sequences for that round+type)
        List<PScore> scoresToMove = new ArrayList<>();
        List<PScore> remainingSourceScores = new ArrayList<>();

        for (PScore score : sourceScores.getScores()) {
            if (roundType.equalsIgnoreCase(score.getType()) && score.getRound() == sourceRound) {
                scoresToMove.add(score);
            } else {
                remainingSourceScores.add(score);
            }
        }

        if (scoresToMove.isEmpty()) {
            result.put("result", "fail");
            result.put("message", "No scores found for that round");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }

        // Determine destination round number
        int destRound = countRoundsForType(destScores, roundType) + 1;

        // Add audit comment timestamp
        String auditComment = String.format("[%s] Round moved from pilot %s",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                sourcePilot.getName());

        // Move scores to destination with new round number
        List<PScore> destScoreList = new ArrayList<>(destScores.getScores());
        for (PScore score : scoresToMove) {
            PScore movedScore = new PScore(destRound, score.getSequence(), score.getScores(), score.getType());
            // Note: audit comment would need to be added to PScore if we want per-score comments
            destScoreList.add(movedScore);
        }
        destScores.setScores(destScoreList);

        // Renumber source pilot's remaining rounds (decrement rounds higher than moved one)
        for (PScore score : remainingSourceScores) {
            if (roundType.equalsIgnoreCase(score.getType()) && score.getRound() > sourceRound) {
                // Create new score with decremented round number
                PScore renumbered = new PScore(score.getRound() - 1, score.getSequence(), score.getScores(), score.getType());
                remainingSourceScores.set(remainingSourceScores.indexOf(score), renumbered);
            }
        }
        sourceScores.setScores(remainingSourceScores);

        // Update active round numbers for both pilots
        int newActiveRound = destRound + 1;
        sourceScores.setActiveRound(roundType, newActiveRound);
        destScores.setActiveRound(roundType, newActiveRound);

        // Save both pilots
        pilotService.savePilotScoresToFile(sourceScores);
        pilotService.savePilotScoresToFile(destScores);

        logger.info("Successfully moved round. Source now has {} {} rounds, dest has {} {} rounds",
                countRoundsForType(sourceScores, roundType), roundType,
                countRoundsForType(destScores, roundType), roundType);

        result.put("result", "ok");
        result.put("message", "Round moved successfully");
        result.put("audit", auditComment);
        return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
    }

    /**
     * Check for available updates by querying GitHub releases API.
     * Compares current version with latest release tag.
     */
    @GetMapping("/api/system/check-update")
    public ResponseEntity<String> checkForUpdate() {
        Map<String, Object> result = new HashMap<>();

        String currentVersion = co.za.imac.judge.JudgeApplication.getAppVersion();
        result.put("currentVersion", currentVersion);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/IMAC-ORG/imac-judge-app/releases/latest"))
                    .header("Accept", "application/vnd.github.v3+json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String latestTag = json.get("tag_name").getAsString();
                // Remove 'v' prefix if present for comparison
                String latestVersion = latestTag.replaceFirst("^v", "");
                boolean updateAvailable = isVersionGreater(latestVersion, currentVersion);

                result.put("latestVersion", latestVersion);
                result.put("latestTag", latestTag);
                result.put("updateAvailable", updateAvailable);
                result.put("result", "ok");

                logger.info("Version check: current={}, latest={}, updateAvailable={}",
                        currentVersion, latestVersion, updateAvailable);

                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
            } else {
                result.put("result", "fail");
                result.put("message", "GitHub API returned status: " + response.statusCode());
                logger.warn("GitHub API check failed with status: {}", response.statusCode());
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.SERVICE_UNAVAILABLE);
            }

        } catch (Exception e) {
            logger.error("Failed to check for updates: {}", e.getMessage());
            result.put("result", "fail");
            result.put("message", "Could not connect to update server. Check internet connection.");
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Run system update using a two-phase approach:
     *
     * 1. Download phase: runs synchronously so we can report the outcome.
     *    The script checks for a newer version and downloads assets to
     *    /tmp/judge-update/. Exit codes: 0 = no update, 1 = error, 2 = ready.
     *
     * 2. Install phase: launched via systemd-run in its own scope so it
     *    survives the judge.service restart. Backs up the current JAR,
     *    installs the update, health-checks, and rolls back on failure.
     */
    @PostMapping("/api/system/update")
    public ResponseEntity<String> runSystemUpdate() {
        Map<String, Object> result = new HashMap<>();

        logger.info("System update requested");

        try {
            // Phase 1: Download (synchronous — we need the exit code)
            int exitCode = runDownloadPhase();

            if (exitCode == 0) {
                // No update was needed - already running latest
                result.put("result", "ok");
                result.put("message", "Already up to date");
                result.put("restart", false);
                logger.info("System already up to date");
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
            } else if (exitCode == 2) {
                // Assets downloaded — notify UI first, then launch install phase
                result.put("result", "ok");
                result.put("message", "Update applied successfully - restarting...");
                result.put("restart", true);
                logger.info("Install phase launched in independent systemd scope");
                launchInstallPhase();
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);
            } else {
                // Error occurred (exit code 1 or other)
                result.put("result", "fail");
                result.put("message", "Update script returned error code: " + exitCode);
                logger.error("Download phase failed with exit code: {}", exitCode);
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            logger.error("System update failed: {}", e.getMessage());
            result.put("result", "fail");
            result.put("message", "Update failed: " + e.getMessage());
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Download phase: check for a newer version and download assets to
     * /tmp/judge-update/. Does NOT stop services or modify installed files.
     *
     * Exit codes (from fetch_update.sh --download-only):
     *   0 = No update needed (already running latest version)
     *   1 = Error occurred during download
     *   2 = Assets downloaded to /tmp/judge-update/ and ready to install
     */
    private int runDownloadPhase() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("/home/judge/fetch_update.sh", "--download-only");
        pb.directory(new File("/home/judge"));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Read output for logging
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("Update [download]: {}", line);
            }
        }

        return process.waitFor();
    }

    /**
     * Install phase: launched via systemd-run so it runs in its own systemd
     * scope, independent of judge.service. When the script calls
     * "systemctl stop judge.service", only the Spring Boot process stops —
     * the install script continues running under its own scope.
     *
     * The script will:
     *   1. Backup current JAR to judge.jar.bak
     *   2. Stop judge.service and kiosk.service
     *   3. Install downloaded assets from /tmp/judge-update/
     *   4. Start services
     *   5. Health check via /actuator/health
     *   6. If healthy: update .judge_last_release, clean up, done
     *   7. If NOT healthy: restore backup, restart services
     *
     * Output is logged to /var/opt/judge/judge-update.log for post-mortem debugging.
     */
    private void launchInstallPhase() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
            "sudo", "systemd-run",
            "--uid=judge",
            "--scope",
            "/home/judge/fetch_update.sh", "--install"
        );
        pb.directory(new File("/home/judge"));
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File("/var/opt/judge/judge-update.log"));
        pb.start();
    }
}
