package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.repository.RessortRepository;
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

    public Ressort getRessortByShortcut(String shortcut){
        return ressortRepository.findByShortcut(shortcut);
    }
}
