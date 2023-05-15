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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    public String showSomeQuestionnaireForm(Authentication authentication) throws UnsupportedEncodingException {
        User user = userService.getUserByUsername(authentication.getName());
        String name;
        Role landRole = roleService.getRoleByName("ROLE_LAND");
        Role ressortRole = roleService.getRoleByName("ROLE_Ressort");

        if(user.getRoles().contains(landRole)){
            name = user.getFederalState().getName();
        }else if(user.getRoles().contains(ressortRole)){
            name = user.getRessort().getName();
        }else{
            name =  federalStateService.getFederalStateByName("Brandenburg").getName();
        }

        return "redirect:/situation/" + URLEncoder.encode(name, "UTF-8");
    }

    @GetMapping("/situation/{name}")
    public String showQuestionnaireForm(@PathVariable String name, Authentication authentication, Model model) throws AccessDeniedException, EntityDoesNotExistException {
        FederalState federalState = federalStateService.getFederalStateByName(name);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByName(name);
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
            }else if(ressort != null) {
                questionnaire = questionnaireService.getQuestionnaireForRessort(ressort);
            }else{
                throw new EntityDoesNotExistException("The Ressort or Federal State does not exist.");
            }
            model.addAttribute("questionnaire", questionnaire);

            model.addAttribute("sectorChangeDetector", new SectorChangeDetector());
            model.addAttribute("name", name);

            return "situation/situation";
        }else{
            if(federalState != null) {
                throw new AccessDeniedException("The user doesnt have the permission to access the federal State.");
            }else{
                throw new AccessDeniedException("The user doesnt have the permission to access the ressort.");
            }
        }
    }

    @PostMapping("/situation/form/{name}")
    public String submitQuestionnaire(@ModelAttribute("questionnaire") Questionnaire questionnaire,
                                      @PathVariable String name, Authentication authentication) throws AccessDeniedException, EntityDoesNotExistException, UnsupportedEncodingException {
        FederalState federalState = federalStateService.getFederalStateByName(name);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByName(name);
        }
        User user = userService.getUserByUsername(authentication.getName());
        Role adminRole = roleService.getRoleByName("ROLE_BBK_ADMIN");
        Role landRole = roleService.getRoleByName("ROLE_LAND");
        Role ressortRole = roleService.getRoleByName("ROLE_RESSORT");

        if(user.getRoles().contains(adminRole) ||
                (user.getRoles().contains(landRole) && user.getFederalState().equals(federalState)) ||
                (user.getRoles().contains(ressortRole) && user.getRessort().equals(ressort))){
            questionnaireService.saveQuestionnaireFromForm(questionnaire, federalState, ressort);
            return "redirect:/situation/" + URLEncoder.encode(name, "UTF-8");
        }else{
            if(federalState != null) {
                throw new AccessDeniedException("The user doesnt have the permission to access the federal State " + federalState.getName() + ".");
            }else{
                throw new AccessDeniedException("The user doesnt have the permission to access the ressort " + ressort.getName() + ".");
            }
        }
    }

    @PostMapping("/situation/{name}")
    public String submitFromFiles(@RequestParam("files") MultipartFile[] files, @PathVariable String name,
                                  Authentication authentication, Model model) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByName(name);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByName(name);
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
            model.addAttribute("name", name);
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