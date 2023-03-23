package de.thb.webbaki.service.questionnaire;

import de.thb.webbaki.entity.questionnaire.Questionnaire;
import de.thb.webbaki.entity.Scenario;
import de.thb.webbaki.entity.questionnaire.UserScenario;
import de.thb.webbaki.enums.ScenarioType;
import de.thb.webbaki.repository.questionnaire.UserScenarioRepository;
import de.thb.webbaki.service.ScenarioService;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
