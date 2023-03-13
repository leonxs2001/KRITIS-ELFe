package de.thb.webbaki.controller;

import de.thb.webbaki.controller.form.ThreatMatrixFormModel;
import de.thb.webbaki.entity.questionnaire.Questionnaire;
import de.thb.webbaki.entity.User;
import de.thb.webbaki.service.Exceptions.NotAuthorizedException;
import de.thb.webbaki.service.questionnaire.QuestionnaireService;
import de.thb.webbaki.service.ScenarioService;
import de.thb.webbaki.service.UserService;
import de.thb.webbaki.service.helper.Counter;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;

@Controller
@AllArgsConstructor
public class SituationController {

    private final QuestionnaireService questionnaireService;
    private final ScenarioService scenarioService;
    private final UserService userService;



    @GetMapping("/situation")
    public String showQuestionnaireForm(Authentication authentication) {
        Long newestQuestionnaireId = questionnaireService.getNewestQuestionnaireByUserId(userService.getUserByUsername(authentication.getName()).getId()).getId();
        return "redirect:/threatmatrix/open/" + newestQuestionnaireId;
    }

    @PostMapping("/situation")
    public String submitQuestionnaire(@ModelAttribute("threatmatrix") @Valid ThreatMatrixFormModel questionnaireFormModel,
                                      Authentication authentication) {
        questionnaireService.saveQuestionnaireFromThreatMatrixFormModel(questionnaireFormModel, userService.getUserByUsername(authentication.getName()));
        return "redirect:/situation/chronic";
    }

    @GetMapping("/situation/open/{questID}")
    public String showThreatMatrixByID(@PathVariable("questID") long questID, Model model, Authentication authentication) throws NotAuthorizedException{
        if(questionnaireService.existsByIdAndUserId(questID,userService.getUserByUsername(authentication.getName()).getId() )){

            Questionnaire quest = questionnaireService.getQuestionnaire(questID);

            questionnaireService.checkIfMatchingWithActiveScenariosFromDB(quest);

            ThreatMatrixFormModel threatMatrixFormModel = new ThreatMatrixFormModel(quest);
            model.addAttribute("threatmatrix", threatMatrixFormModel);
            model.addAttribute("value", new Counter());

            model.addAttribute("counter", new Counter());

            return "situation/create_threatmatrix";
        }else{
            throw new NotAuthorizedException("This user could not access this questionnaire.");
        }
    }


    @Transactional
    @GetMapping(path = "/situation/chronic/{questID}")
    public String deleteQuestionnaireByID(@PathVariable("questID") long questID){
        questionnaireService.deleteQuestionnaireById(questID);

        return "redirect:/situation/chronic";
    }

    @GetMapping("/situation/chronic")
    public String showQuestChronic(Authentication authentication,Model model) {

        if (userService.getUserByUsername(authentication.getName()) != null) {
            User user = userService.getUserByUsername(authentication.getName());
            final var questList = questionnaireService.getAllQuestByUser(user.getId());
            model.addAttribute("questList", questList);

        }
        return "situation/chronic";
    }


}