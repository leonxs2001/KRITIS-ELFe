package de.thb.kritis_elfe.service.questionnaire;

import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.entity.Scenario;
import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.repository.questionnaire.FilledScenarioRepository;
import de.thb.kritis_elfe.service.ScenarioService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class FilledScenarioService {
    private final FilledScenarioRepository filledScenarioRepository;
    private final ScenarioService scenarioService;

    public void saveAllFilledScenarios(List<FilledScenario> userScenarios){
        filledScenarioRepository.saveAll(userScenarios);}

    public void updateFilledScenarioValueAndCommentById(short value, String comment, long id){
        filledScenarioRepository.updateFilledScenarioValueAndCommentDateFromId(value, comment ,id);
    }
    public FilledScenario saveFilledScenario(FilledScenario userScenario){
        return filledScenarioRepository.save(userScenario);
    }

}
