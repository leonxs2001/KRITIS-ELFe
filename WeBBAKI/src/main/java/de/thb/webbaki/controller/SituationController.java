package de.thb.webbaki.controller;

import de.thb.webbaki.entity.Sector;
import de.thb.webbaki.entity.questionnaire.Questionnaire;
import de.thb.webbaki.service.SectorService;
import de.thb.webbaki.service.helper.SectorChangeDetector;
import de.thb.webbaki.service.questionnaire.QuestionnaireService;
import de.thb.webbaki.service.ScenarioService;
import de.thb.webbaki.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
public class SituationController {

    private final QuestionnaireService questionnaireService;
    private final ScenarioService scenarioService;
    private final UserService userService;
    private final SectorService sectorService;

    @GetMapping("/situation")
    public String showQuestionnaireForm(Authentication authentication, Model model) {
        Questionnaire questionnaire = questionnaireService.getQuestionnaireForUser(userService.getUserByUsername(authentication.getName()));

        model.addAttribute("questionnaire", questionnaire);

        List<Sector> sectors = sectorService.getAllSectors();
        model.addAttribute("sectors", sectors);
        model.addAttribute("sectorChangeDetector", new SectorChangeDetector());


        return "situation/situation";
    }

    @PostMapping("/situation")
    public String submitQuestionnaire(@ModelAttribute("questionnaire") Questionnaire questionnaire,
                                      Authentication authentication) {
        questionnaireService.saveQuestionnaireFromForm(questionnaire, userService.getUserByUsername(authentication.getName()));
        return "redirect:situation";
    }

}