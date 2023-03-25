package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Role;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.AccessDeniedException;
import de.thb.kritis_elfe.service.helper.SectorChangeDetector;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@AllArgsConstructor
public class SituationController {

    private final QuestionnaireService questionnaireService;
    private final ScenarioService scenarioService;
    private final UserService userService;
    private final SectorService sectorService;
    private final FederalStateService federalStateService;
    private final RoleService roleService;

    @GetMapping("/situation")
    public String showSomeQuestionnaireForm(Authentication authentication){
        User user = userService.getUserByUsername(authentication.getName());
        FederalState federalState;
        Role landRole = roleService.getRoleByName("ROLE_LAND");

        if(user.getRoles().contains(landRole)){
            federalState = user.getFederalState();
        }else{
            federalState = federalStateService.getFederalStateByName("Brandenburg");
        }

        return "redirect:/situation/" + federalState.getShortcut();
    }

    @GetMapping("/situation/{federalStateShortcut}")
    public String showQuestionnaireForm(@PathVariable String federalStateShortcut, Authentication authentication, Model model) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByShortcut(federalStateShortcut);
        User user = userService.getUserByUsername(authentication.getName());
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");

        if(user.getRoles().contains(adminRole) || user.getFederalState().equals(federalState)){
            Questionnaire questionnaire = questionnaireService.getQuestionnaireForFederalState(federalState);

            model.addAttribute("questionnaire", questionnaire);

            model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
            model.addAttribute("federalStateShortcut", federalStateShortcut);

            return "situation/situation";
        }else{
            throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
        }
    }

    @PostMapping("/situation/form/{federalStateShortcut}")
    public String submitQuestionnaire(@ModelAttribute("questionnaire") Questionnaire questionnaire,
                                      @PathVariable String federalStateShortcut, Authentication authentication) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByShortcut(federalStateShortcut);
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");
        User user = userService.getUserByUsername(authentication.getName());

        if(user.getRoles().contains(adminRole) || user.getFederalState().equals(federalState)) {
            questionnaireService.saveQuestionnaireFromForm(questionnaire);
            return "redirect:/situation/" + federalStateShortcut;
        }else{
            throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
        }
    }

    @PostMapping("/situation/{federalStateShortcut}")
    public String submitFilesFromForm(@RequestParam("files") MultipartFile[] files, @PathVariable String federalStateShortcut,
                                      Authentication authentication, Model model) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByShortcut(federalStateShortcut);
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");
        User user = userService.getUserByUsername(authentication.getName());

        if(user.getRoles().contains(adminRole) || user.getFederalState().equals(federalState)) {
            Questionnaire questionnaire = questionnaireService.saveQuestionnaireFromFiles(files, federalState, model);
            model.addAttribute("questionnaire", questionnaire);
            model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
            return "situation/situation";
        }else{
            throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
        }
    }

}