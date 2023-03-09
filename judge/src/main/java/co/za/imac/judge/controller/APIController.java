package co.za.imac.judge.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.service.CompService;

@RestController
public class APIController {
    @Autowired
    private CompService compService;
    @PostMapping("/api/comp")
	public CompDTO createEmployee(@RequestBody CompDTO comp) throws IOException {
		return compService.createComp(comp);
	}
}
