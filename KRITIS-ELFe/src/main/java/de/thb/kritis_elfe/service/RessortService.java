package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.controller.form.RessortsForm;
import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.repository.RessortRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RessortService {
    private final RessortRepository ressortRepository;

    public List<Ressort> getAllRessorts(){return ressortRepository.findAll();}

    public Ressort getRessortByName(String name){
        return ressortRepository.findByName(name);
    }

    public Ressort createRessort(Ressort ressort){return ressortRepository.save(ressort);}

    /**
     * Saves the ressort from given form and deletes all missing ressorts.
     * @param ressortsForm
     */
    public void resetRessortsByRessortsForm(RessortsForm ressortsForm){
        for(Ressort ressort: ressortRepository.findAll()){
            if(!ressortsForm.getRessorts().contains(ressort)){
                ressortRepository.delete(ressort);
            }
        }
        ressortRepository.saveAll(ressortsForm.getRessorts());
    }
}
