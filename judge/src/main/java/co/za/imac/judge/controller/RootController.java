package co.za.imac.judge.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.service.CompService;

@Controller
public class RootController {

    @Autowired
    private CompService compService;

    @GetMapping("/")
	public String home(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) throws IOException {

        System.out.println("Is there a comp? : " + compService.isCurrentComp());
        if(!compService.isCurrentComp()){
            System.out.println("Theres no current comp!!");
            return "redirect:/newcomp";
        }
		model.addAttribute("name", name);
		return "index";
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
