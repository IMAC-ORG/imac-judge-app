package co.za.imac.judge.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
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

    @PostMapping("/api/sequence/upload")
    public ResponseEntity<String> uploadSequenceZip(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "expectedFolders", required = false) List<String> expectedFolders) {

        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("result", "fail");
            result.put("message", "No file provided.");
            result.put("retry", true);
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            result.put("result", "fail");
            result.put("message", "File must be a ZIP archive.");
            result.put("retry", true);
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
        }

        // Target directory: {configPath}/figures/en/event/
        Path eventDir = Paths.get(SettingUtils.getApplicationConfigPath(), "figures", "en", "event");

        try {
            // Ensure event directory exists
            Files.createDirectories(eventDir);

            Set<String> extractedFolders = new HashSet<>();

            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();

                    // Security: prevent path traversal attacks
                    if (entryName.contains("..")) {
                        logger.warn("Skipping suspicious entry: {}", entryName);
                        continue;
                    }

                    // Parse the entry path
                    String[] parts = entryName.replace('\\', '/').split("/");

                    // Skip files at root level (only process folders and their contents)
                    if (parts.length < 1 || (parts.length == 1 && !entry.isDirectory())) {
                        continue;
                    }

                    String topLevelFolder = parts[0];
                    if (topLevelFolder.isEmpty()) {
                        continue;
                    }

                    extractedFolders.add(topLevelFolder);

                    // Build target path
                    Path targetPath = eventDir.resolve(entryName);

                    if (entry.isDirectory()) {
                        Files.createDirectories(targetPath);
                    } else {
                        // Ensure parent directories exist
                        Files.createDirectories(targetPath.getParent());
                        Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    zis.closeEntry();
                }
            }

            if (extractedFolders.isEmpty()) {
                result.put("result", "fail");
                result.put("message", "No folders found in ZIP file.");
                result.put("retry", true);
                return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
            }

            // Verify expected folders if provided
            if (expectedFolders != null && !expectedFolders.isEmpty()) {
                List<String> missingFolders = new ArrayList<>();
                for (String expected : expectedFolders) {
                    if (!extractedFolders.contains(expected)) {
                        missingFolders.add(expected);
                    }
                }

                if (!missingFolders.isEmpty()) {
                    result.put("result", "fail");
                    result.put("message", "Missing expected folders: " + String.join(", ", missingFolders));
                    result.put("extractedFolders", extractedFolders);
                    result.put("retry", true);
                    return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.BAD_REQUEST);
                }
            }

            result.put("result", "ok");
            result.put("message", "Upload complete. Extracted " + extractedFolders.size() + " folder(s).");
            result.put("extractedFolders", extractedFolders);
            result.put("retry", false);

            logger.info("Sequence upload complete. Extracted folders: {}", extractedFolders);
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.OK);

        } catch (IOException e) {
            logger.error("Failed to extract ZIP file: {}", e.getMessage());
            result.put("result", "fail");
            result.put("message", "Failed to extract ZIP: " + e.getMessage());
            result.put("retry", true);
            return new ResponseEntity<>(new Gson().toJson(result), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
