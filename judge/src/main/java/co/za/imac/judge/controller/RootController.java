package co.za.imac.judge.controller;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.dto.Pilot;
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
	public String home(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) throws IOException, ParserConfigurationException, SAXException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
        List<Pilot> pilots = pilotService.getPilots();
        if(!compService.isCurrentComp()){
            System.out.println("Theres no current comp!!");
            return "redirect:/newcomp";
        }
		model.addAttribute("pilots", pilots);
		return "index";
	}
    @GetMapping("/judge")
	public String judge(@RequestParam(name="pilot_id", required=false, defaultValue="World") String name, Model model) throws IOException, ParserConfigurationException, SAXException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
        if(!compService.isCurrentComp()){
            System.out.println("Theres no current comp!!");
            return "redirect:/newcomp";
        }
        sequenceService.getAllSequences();
		return "judge";
	}
    
    @GetMapping("/newcomp")
	public String newcomp(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) throws IOException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
        if(compService.isCurrentComp()){
            System.out.println("Theres is a current comp!!");
            return "redirect:/";
        }
		model.addAttribute("name", name);
		return "newcomp";
	}
}
