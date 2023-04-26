package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.Sector;
import de.thb.kritis_elfe.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorService {
    @Autowired
    private SectorRepository sectorRepository;

    public List<Sector> getAllSectors(){return sectorRepository.findAll();}
    public Sector getSectorByName(String name){return sectorRepository.findByName(name);}
    public Optional<Sector> getSectorById(long id){return sectorRepository.findById(id);}
    public Sector createSector(Sector sector){return sectorRepository.save(sector);}
}
