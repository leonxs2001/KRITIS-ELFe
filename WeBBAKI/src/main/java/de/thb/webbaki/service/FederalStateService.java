package de.thb.webbaki.service;

import de.thb.webbaki.entity.FederalState;
import de.thb.webbaki.repository.FederalStateRepository;
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
