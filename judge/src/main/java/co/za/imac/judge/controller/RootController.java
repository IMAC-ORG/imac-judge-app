package co.za.imac.judge.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

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
import co.za.imac.judge.service.CompService;
import co.za.imac.judge.service.PilotService;
import co.za.imac.judge.service.SequenceService;

@Controller
public class RootController {

    @Autowired
    private CompService compService;
    @Autowired
    private PilotService pilotService;
    @Autowired
    private SequenceService sequenceService;

    @GetMapping("/")
	public String home(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) throws IOException, ParserConfigurationException, SAXException, UnirestException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
        List<Pilot> pilots = pilotService.getPilots();
        if(!compService.isCurrentComp()){
            System.out.println("Theres no current comp!!");
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
		return "index";
	}
    @GetMapping("/judge")
	public String judge(@RequestParam(name="pilot_id", required=true) int pilot_id, @RequestParam(name="roundType", required=true) String roundType, @RequestParam(name="dirflip", required=true, defaultValue = "false") Boolean dirflip, Model model) throws IOException, ParserConfigurationException, SAXException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
        if(!compService.isCurrentComp()){
            System.out.println("Theres no current comp!!");
            return "redirect:/newcomp";
        }
        Pilot pilot = pilotService.getPilot(pilot_id);
        System.out.println("Pilot data");
        System.out.println(new Gson().toJson(pilot));
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
        System.out.println(new Gson().toJson(sequences));
        model.addAttribute("dirletter", (dirflip==true ? "C" : "B"));
		return "judge";
	}
    
    @GetMapping("/newcomp")
	public String newcomp(Model model) throws IOException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
		model.addAttribute("isCurrentComp", compService.isCurrentComp());
        if(compService.isCurrentComp()){
            model.addAttribute("currentRounds", compService.getComp().getRounds());
            model.addAttribute("currentSequences", compService.getComp().getSequences());
            model.addAttribute("currentUnknownSequences", compService.getComp().getUnknown_sequences());
        }else{
            model.addAttribute("currentRounds", 2);
            model.addAttribute("currentSequences", 2);
            model.addAttribute("currentUnknownSequences", 0);
        }
		return "newcomp";
	}
}
