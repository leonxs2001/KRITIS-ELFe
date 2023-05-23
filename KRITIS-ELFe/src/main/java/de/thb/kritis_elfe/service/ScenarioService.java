package de.thb.kritis_elfe.service;


import de.thb.kritis_elfe.entity.Scenario;
import de.thb.kritis_elfe.repository.ScenarioRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Builder
@AllArgsConstructor
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;

    public Scenario getScenarioByDescriptionAndActive(String description){
        return scenarioRepository.findByDescriptionAndActive(description, true);
    }

    public List<Scenario> getAllScenariosByActiveTrue(){return scenarioRepository.findByActive(true);}

    public Scenario createScenario(Scenario scenario){return scenarioRepository.save(scenario);}
}
