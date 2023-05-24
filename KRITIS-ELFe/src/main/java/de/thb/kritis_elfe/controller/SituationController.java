package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.AccessDeniedException;
import de.thb.kritis_elfe.service.Exceptions.EntityDoesNotExistException;
import de.thb.kritis_elfe.service.helper.SectorChangeDetector;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@AllArgsConstructor
public class SituationController {

    private final QuestionnaireService questionnaireService;
    private final UserService userService;

    @GetMapping("/lagebericht")
    public String showSomeQuestionnaireForm(Authentication authentication) throws UnsupportedEncodingException {
        User user = userService.getUserByUsername(authentication.getName());
        String name = userService.getRessortOrFederalStateName(user);

        return "redirect:/lagebericht/" + URLEncoder.encode(name, "UTF-8");
    }

    @GetMapping("/lagebericht/{name}")
    public String showQuestionnaireForm(@PathVariable String name, Authentication authentication, Model model) throws AccessDeniedException, EntityDoesNotExistException {
        Questionnaire questionnaire = questionnaireService.getQuestionnaireFromCreatorsName(name, userService.getUserByUsername(authentication.getName()));

        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
        model.addAttribute("name", name);

        return "situation";
    }

    @PostMapping("/lagebericht/form/{name}")
    public String submitQuestionnaire(@ModelAttribute("questionnaire") Questionnaire questionnaire,
                                      @PathVariable String name, Authentication authentication) throws AccessDeniedException, EntityDoesNotExistException, UnsupportedEncodingException {
        questionnaireService.saveQuestionnaireFromForm(questionnaire, name, userService.getUserByUsername(authentication.getName()));
        return "redirect:/lagebericht/" + URLEncoder.encode(name, "UTF-8");
    }

    @PostMapping("/lagebericht/{name}")
    public String submitFromFiles(@RequestParam("files") MultipartFile[] files, @PathVariable String name,
                                  Authentication authentication, Model model) throws AccessDeniedException {
        Questionnaire questionnaire = questionnaireService.saveQuestionnaireFromFiles(files, name, userService.getUserByUsername(authentication.getName()), model);
        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
        model.addAttribute("name", name);
        return "situation";
    }

}