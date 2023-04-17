package co.za.imac.judge.controller;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.dto.PilotScoreDTO;
import co.za.imac.judge.dto.PilotScores;
import co.za.imac.judge.service.CompService;
import co.za.imac.judge.service.PilotService;
import co.za.imac.judge.service.SequenceService;

@RestController
public class APIController {
    @Autowired
    private CompService compService;
    @Autowired
    private PilotService pilotService;
    @Autowired
    private SequenceService sequenceService;

    @PostMapping("/api/comp")
    public CompDTO createEmployee(@RequestBody CompDTO comp,
            @RequestParam(name = "edit", required = true) Boolean editComp)
            throws IOException, ParserConfigurationException, SAXException {
        System.out.println(new Gson().toJson(comp));
        // fetch pilots
        pilotService.getPilotsFileFromScore();
        if (!editComp) {
            pilotService.setupPilotScores();
        }
        sequenceService.getSequenceFileFromScore();
        return compService.createComp(comp);
    }

    @GetMapping("/api/pilots/sync")
    public String syncPilots() throws IOException, ParserConfigurationException, SAXException {
        // fetch pilots
        pilotService.getPilotsFileFromScore();
        sequenceService.getSequenceFileFromScore();
        // pilotService.setupPilotScores();
        return "{'sync':'ok'}";
    }

    @GetMapping("/api/scores/sync")
    public String syncScores() throws Exception {
        // fetch pilots
        pilotService.syncPilotsToScoreWebService();
        return "{'sync':'ok'}";
    }

    @PostMapping("/api/score")
    public PilotScores submitScores(@RequestBody PilotScoreDTO pilotScoreDTO)
            throws ParserConfigurationException, SAXException, IOException {
        System.out.println(new Gson().toJson(pilotScoreDTO));
        return pilotService.submitScore(pilotScoreDTO);
    }
}
