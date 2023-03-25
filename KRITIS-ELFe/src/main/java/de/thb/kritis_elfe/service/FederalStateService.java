package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.repository.FederalStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FederalStateService {
    @Autowired
    private FederalStateRepository federalStateRepository;

    public List<FederalState> getAllFederalStates(){return federalStateRepository.findAll();}

    public FederalState getFederalStateByName(String name){
        return federalStateRepository.findByName(name);
    }

    public FederalState getFederalStateByShortcut(String shortcut){return federalStateRepository.findByShortcut(shortcut);}

    public FederalState createFederalState(FederalState federalState){return federalStateRepository.save(federalState);}

}
