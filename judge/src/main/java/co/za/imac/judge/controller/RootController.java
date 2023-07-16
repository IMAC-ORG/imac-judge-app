package co.za.imac.judge.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import co.za.imac.judge.dto.RoundDTO;
import co.za.imac.judge.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.mashape.unirest.http.exceptions.UnirestException;

import co.za.imac.judge.dto.FigureDTO;
import co.za.imac.judge.dto.Pilot;
import co.za.imac.judge.dto.PilotScores;

@Controller
public class RootController {
    private static final Logger logger =
            LoggerFactory.getLogger(RootController.class);

    @Autowired
    private CompService compService;
    @Autowired
    private RoundsService roundService;
    @Autowired
    private PilotService pilotService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SettingService settingService;

    @GetMapping("/")
    public String home(Model model)
            throws IOException, ParserConfigurationException, SAXException, UnirestException {

        // Check if we have seen the splash-screen.
        if (settingService.isFirstRun()) {
            logger.info("This is our first run.");
            settingService.setFirstRun(false);
            return "index";
        }


        /**********
         *  Ok.   Lets check out the stuff we *need* to proceed.
         *
         *  settings:      settings.json via settingsService.getSettings().
         *                 We dont need to check this because we create an empty
         *                 one if it does not exist.
         *
         *  comp:          If there's no file, then we ask to create one.
         *                 Score wont tell us how many unknown rounds to score
         *                 so we need to have it configured.
         *
         *  pilots:        Fail if we cant get it or it does not exist.
         *
         *  sequences:     Fail if we cant get it or it does not exist.
         *
         *  contest_prefs: We only need it for name (now and then it's not that important).
         */

        settingService.getSettings();
        logger.info("Is there a comp? : " + compService.isCurrentComp());

        List<Pilot> pilots = null;
        try {
            pilots = pilotService.getPilots();
            sequenceService.getAllSequences();  // We're not doing anything with it just yet.
        } catch (Exception e) {
            try {
                logger.error("Could not get contest data.  " + e.getMessage());
                return"/needscore";
            } catch (Exception logger_e) {
                logger_e.printStackTrace();
            }
        }

        if(!compService.isCurrentComp()){
            logger.debug("Redirect to newcomp page.");
            return "redirect:/newcomp";
        }

        // Now if we have a comp, are we scoring a round?
        logger.info("Are we scoring a round?? : " + roundService.isScoringRound());
        if(!roundService.isScoringRound()){
            logger.debug("Redirect to new round page.");
            return "redirect:/rounds";
        }

        if ("global".equalsIgnoreCase(compService.getComp().getScore_mode()))
            return "redirect:/pilot-list-global";
        else
            return "redirect:/rounds";
    }

    @GetMapping("/pilot-list-global")
    public String pilotListGlobal(Model model)
            throws IOException, ParserConfigurationException, SAXException, UnirestException {

        // Check first if we have a valid comp
        logger.info("Is there a comp? : " + compService.isCurrentComp());
        List<Pilot> pilots = pilotService.getPilots();
        if(!compService.isCurrentComp()){
            logger.debug("Redirect to newcomp page.");
            return "redirect:/newcomp";
        }

        model.addAttribute("pilots", pilots);
        HashMap<Integer,PilotScores> pilotScores = new HashMap<>();


        for(Pilot pilot : pilots){
            pilotScores.put(pilot.getPrimary_id(), pilotService.getPilotScores(pilot));
        }
        model.addAttribute("pilotScores", pilotScores);
        model.addAttribute("comp", compService.getComp());
        model.addAttribute("dirflip", false);
        return "pilot-list-global";
    }

    @GetMapping("/pilot-list-round")
    public String pilotListRound(Model model)
            throws IOException, ParserConfigurationException, SAXException, UnirestException {
        /******************
         * Create the carousel for the pilot selector.
         * Before we send the list to the frontend, filter it first.
         *
         * We can only do that if we have a clear active round.
         */
        // Check first if we have a valid comp
        logger.info("Is there a comp? : " + compService.isCurrentComp());
        List<Pilot> pilots = pilotService.getPilots();
        if(!compService.isCurrentComp()){
            logger.debug("Redirect to newcomp page.");
            return "redirect:/newcomp";
        }

        // Now if we have a comp, are we scoring a round?
        RoundDTO roundToScore = roundService.getScoringRound();
        logger.info("Are we scoring a round?? : " + (roundToScore != null));
        if (roundToScore == null) {
            // Tell them to choose a round to Score.
            logger.debug("Redirect to new round page.");
            return "redirect:/rounds";
        }

        List<Pilot> filteredPilots = new ArrayList<>();
        for (Pilot p : pilots) {
            // If pilot is registered for FreeStyle then add him.
            if ("FREESTYLE".equalsIgnoreCase(roundToScore.getType()) && Boolean.TRUE.equals(p.getFreestyle())) {
                filteredPilots.add(p);
            }
            if (p.getClassString() != null &&  p.getClassString().equalsIgnoreCase(roundToScore.getComp_class())) {
                filteredPilots.add(p);
            }
        }

        model.addAttribute("pilots", filteredPilots);
        HashMap<Integer,PilotScores> pilotScores = new HashMap<>();
        for(Pilot pilot : filteredPilots){
            pilotScores.put(pilot.getPrimary_id(), pilotService.getPilotScores(pilot));
        }
        model.addAttribute("pilotScores", pilotScores);
        model.addAttribute("comp", compService.getComp());
        return "pilot-list-round";
    }

    @GetMapping("/judge")
	public String judge(@RequestParam(name="pilot_id", required=true) int pilot_id, @RequestParam(name="roundType", required=true) String roundType, @RequestParam(name="dirflip", required=true, defaultValue = "false") Boolean dirflip, Model model) throws IOException, ParserConfigurationException, SAXException {

        logger.info("Is there a comp? : " + compService.isCurrentComp());
        if(!compService.isCurrentComp()){
            logger.debug("Redirect to newcomp page.");
            return "redirect:/newcomp";
        }
        Pilot pilot = pilotService.getPilot(pilot_id);
        logger.debug("Pilot data:");
        logger.debug(new Gson().toJson(pilot));
        PilotScores pilotScores = pilotService.getPilotScores(pilot);
        
        List<FigureDTO> sequences =  sequenceService.getAllSequenceForClass(pilot.getClassString().toUpperCase(), roundType.toUpperCase());
        if(sequences == null || sequences.size() == 0 || sequences.isEmpty()){
            return "noseq";
        }
        model.addAttribute("maneuvers", sequences);
        model.addAttribute("numOfManeuvers", sequences.size());
        model.addAttribute("pilot", pilot);
        model.addAttribute("pilotScores", pilotScores);
        model.addAttribute("roundType", roundType.toUpperCase());
        model.addAttribute("pilot_class", pilot.getClassString());
        String sequencesJson =  new Gson().toJson(sequences);
        model.addAttribute("sequencesjson",sequencesJson);
        model.addAttribute("dirletter", (dirflip==true ? "C" : "B"));
        logger.debug("Sequence data:");
        logger.debug(new Gson().toJson(sequences));
		return "judge";
	}
    
    @GetMapping("/newcomp")
	public String newcomp(Model model) throws IOException {

        boolean isComp = compService.isCurrentComp();
        logger.info("Is there a comp? : " + isComp);
        model.addAttribute("isCurrentComp", isComp);
        if(isComp){
            model.addAttribute("compName", compService.getComp().getComp_name());
            model.addAttribute("compId", compService.getComp().getComp_id());
            model.addAttribute("scoreMode", compService.getComp().getScore_mode());
            model.addAttribute("maxSeqPerRound", compService.getComp().getSequences());
            model.addAttribute("maxUnknownSeqPerRound", compService.getComp().getUnknown_sequences());
        }else{
            model.addAttribute("compName", "Untitled Comp");
            model.addAttribute("compId", 0);
            model.addAttribute("scoreMode", "byRound");
            model.addAttribute("maxSeqPerRound", 2);
            model.addAttribute("maxUnknownSeqPerRound", 1);
        }
		return "newcomp";
	}

    @GetMapping("/rounds")
    public String showRounds(@RequestParam(defaultValue = "completed", required=false) String mode, Model model) throws IOException {

        model.addAttribute("isCurrentComp", compService.isCurrentComp());
        model.addAttribute("isScoringRound", roundService.isScoringRound());
        model.addAttribute("rounds", roundService.getRounds());
        model.addAttribute("schedules", scheduleService.getSchedules());
        if(compService.isCurrentComp()){

            model.addAttribute("scoreMode", compService.getComp().getScore_mode());
            model.addAttribute("maxSeqPerRound", compService.getComp().getSequences());
            model.addAttribute("maxUnknownSeqPerRound", compService.getComp().getUnknown_sequences());

            switch (compService.getComp().getScore_mode()) {
                case "byRound":
                    if (roundService.isScoringRound()) {
                        model.addAttribute("currentScoringRound", roundService.getScoringRound());
                        logger.info("scoreMode byRound : Redirecting to pilot-list-round tpo score active round.");
                        return "redirect:/pilot-list-round";
                    } else {
                        // Lets enter a new round to score.
                        logger.info("scoreMode byRound : Redirecting to new round page.");
                        return "redirect:/newround";
                    }
                case "flightline":
                    if (roundService.isScoringRound()) {
                        model.addAttribute("currentScoringRound", roundService.getScoringRound());
                        logger.info("scoreMode flightline : Redirecting to pilot-list-round");
                        return "redirect:/pilot-list-round";
                    } else {
                        // We have to either choose or wait for a round to be chosen for us.
                        logger.info("scoreMode flightline : No active round yet, nothing to do.");
                        return "/rounds";
                    }
                case "global":
                    logger.info("scoreMode global : Redirecting to pilot-list-global");
                    return "redirect:/pilot-list-global";
                default:
                    logger.error("Invalid score mode " + compService.getComp().getScore_mode());
                    return "redirect:/newcomp";
            }
        } else {
            model.addAttribute("scoreMode", "byRound");
            model.addAttribute("maxSeqPerRound", 2);
            model.addAttribute("maxUnknownSeqPerRound", 1);
        }
        return "rounds";
    }

    @GetMapping("/newround")
    public String newRound(Model model) throws IOException {

        // We need to send the list of rounds and available schedules to the page.
        model.addAttribute("rounds", roundService.getRounds());
        model.addAttribute("schedules", scheduleService.getSchedules());
        logger.debug("Schedule Count: " + scheduleService.getSchedules().size());
        return "newround";
    }
}
