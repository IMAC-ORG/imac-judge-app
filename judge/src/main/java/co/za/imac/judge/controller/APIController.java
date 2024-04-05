package co.za.imac.judge.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import co.za.imac.judge.service.CompService;
import co.za.imac.judge.service.PilotService;
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
}
