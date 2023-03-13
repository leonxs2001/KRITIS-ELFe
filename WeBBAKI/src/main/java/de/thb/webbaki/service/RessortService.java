package de.thb.webbaki.service;

import de.thb.webbaki.entity.FederalState;
import de.thb.webbaki.entity.Ressort;
import de.thb.webbaki.repository.FederalStateRepository;
import de.thb.webbaki.repository.RessortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RessortService {
    @Autowired
    private RessortRepository ressortRepository;

    public List<Ressort> getAllRessorts(){return ressortRepository.findAll();}

    public Ressort getRessortByName(String name){
        return ressortRepository.findByName(name);
    }

    public Ressort createRessort(Ressort ressort){return ressortRepository.save(ressort);}

}
