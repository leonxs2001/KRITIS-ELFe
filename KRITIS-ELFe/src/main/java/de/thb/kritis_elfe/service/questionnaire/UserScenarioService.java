package de.thb.kritis_elfe.service.questionnaire;

import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.entity.Scenario;
import de.thb.kritis_elfe.entity.questionnaire.UserScenario;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.repository.questionnaire.UserScenarioRepository;
import de.thb.kritis_elfe.service.ScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserScenarioService {
    @Autowired
    private UserScenarioRepository userScenarioRepository;
    @Autowired
    private ScenarioService scenarioService;

    public void saveAllUserScenario(List<UserScenario> userScenarios){userScenarioRepository.saveAll(userScenarios);}

    public void updateUserScenarioValueAndCommentById(short value, String comment, long id){
        userScenarioRepository.updateUserScenarioValueAndCommentDateFromId(value, comment ,id);
    }
    public UserScenario saveUserScenario(UserScenario userScenario){
        return userScenarioRepository.save(userScenario);
    }


    public List<UserScenario> createUserScenariosForQuestionnaire(Questionnaire questionnaire){
        List<UserScenario> userScenarios = new ArrayList<>();
        List<Scenario> scenarios = scenarioService.getAllScenariosByActiveTrue();
        for(Scenario scenario: scenarios){
            UserScenario userScenario = UserScenario.builder().
                    scenario(scenario).
                    //questionnaire(questionnaire).
                    comment("").build();
            if(scenario.getScenarioType() == ScenarioType.AUSWAHL){
                userScenario.setValue((short)0);
            }
            userScenarios.add(userScenario);
        }

        return userScenarios;
    }

}
