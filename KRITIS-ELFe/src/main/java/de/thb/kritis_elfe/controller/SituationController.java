package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.entity.Role;
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

@Controller
@AllArgsConstructor
public class SituationController {

    private final QuestionnaireService questionnaireService;
    private final ScenarioService scenarioService;
    private final UserService userService;
    private final SectorService sectorService;
    private final FederalStateService federalStateService;
    private final RoleService roleService;
    private final RessortService ressortService;

    @GetMapping("/situation")
    public String showSomeQuestionnaireForm(Authentication authentication){
        User user = userService.getUserByUsername(authentication.getName());
        String shortcut;
        Role landRole = roleService.getRoleByName("ROLE_LAND");
        Role ressortRole = roleService.getRoleByName("ROLE_Ressort");

        if(user.getRoles().contains(landRole)){
            shortcut = user.getFederalState().getShortcut();
        }else if(user.getRoles().contains(ressortRole)){
            shortcut = user.getRessort().getShortcut();
        }else{
            shortcut =  federalStateService.getFederalStateByName("Brandenburg").getShortcut();
        }

        return "redirect:/situation/" + shortcut;
    }

    @GetMapping("/situation/{shortcut}")
    public String showQuestionnaireForm(@PathVariable String shortcut, Authentication authentication, Model model) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByShortcut(shortcut);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByShortcut(shortcut);
        }
        User user = userService.getUserByUsername(authentication.getName());
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");
        Role landRole = roleService.getRoleByName("ROLE_LAND");
        Role ressortRole = roleService.getRoleByName("ROLE_RESSORT");

        if(user.getRoles().contains(adminRole) ||
                (user.getRoles().contains(landRole) && user.getFederalState().equals(federalState)) ||
                (user.getRoles().contains(ressortRole) && user.getRessort().equals(ressort))){
            Questionnaire questionnaire;
            if(federalState != null) {
                questionnaire = questionnaireService.getQuestionnaireForFederalState(federalState);
            }else{
                questionnaire = questionnaireService.getQuestionnaireForRessort(ressort);
            }
            model.addAttribute("questionnaire", questionnaire);

            model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
            model.addAttribute("shortcut", shortcut);

            return "situation/situation";
        }else{
            if(federalState != null) {
                throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
            }else{
                throw new AccessDeniedException("The user doesnt have the permission to access the ressort " + ressort.getName() + ".");
            }
        }
    }

    @PostMapping("/situation/form/{shortcut}")
    public String submitQuestionnaire(@ModelAttribute("questionnaire") Questionnaire questionnaire,
                                      @PathVariable String shortcut, Authentication authentication) throws AccessDeniedException, EntityDoesNotExistException {
        FederalState federalState = federalStateService.getFederalStateByShortcut(shortcut);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByShortcut(shortcut);
        }
        User user = userService.getUserByUsername(authentication.getName());
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");
        Role landRole = roleService.getRoleByName("ROLE_LAND");
        Role ressortRole = roleService.getRoleByName("ROLE_RESSORT");

        if(user.getRoles().contains(adminRole) ||
                (user.getRoles().contains(landRole) && user.getFederalState().equals(federalState)) ||
                (user.getRoles().contains(ressortRole) && user.getRessort().equals(ressort))){
            questionnaireService.saveQuestionnaireFromForm(questionnaire, federalState, ressort);
            return "redirect:/situation/" + shortcut;
        }else{
            if(federalState != null) {
                throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
            }else{
                throw new AccessDeniedException("The user doesnt have the permission to access the ressort " + ressort.getName() + ".");
            }
        }
    }

    @PostMapping("/situation/{shortcut}")
    public String submitFilesFromForm(@RequestParam("files") MultipartFile[] files, @PathVariable String shortcut,
                                      Authentication authentication, Model model) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByShortcut(shortcut);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByShortcut(shortcut);
        }
        User user = userService.getUserByUsername(authentication.getName());
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");
        Role landRole = roleService.getRoleByName("ROLE_LAND");
        Role ressortRole = roleService.getRoleByName("ROLE_RESSORT");

        if(user.getRoles().contains(adminRole) ||
                (user.getRoles().contains(landRole) && user.getFederalState().equals(federalState)) ||
                (user.getRoles().contains(ressortRole) && user.getRessort().equals(ressort))){
            Questionnaire questionnaire = questionnaireService.saveQuestionnaireFromFiles(files, federalState, ressort, model);
            model.addAttribute("questionnaire", questionnaire);
            model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
            model.addAttribute("shortcut", shortcut);
            return "situation/situation";
        }else{
            if(federalState != null) {
                throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
            }else{
                throw new AccessDeniedException("The user doesnt have the permission to access the ressort " + ressort.getName() + ".");
            }
        }
    }

}