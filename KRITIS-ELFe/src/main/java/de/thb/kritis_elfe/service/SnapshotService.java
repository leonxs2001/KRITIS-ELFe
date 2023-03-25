package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.Snapshot;
import de.thb.kritis_elfe.repository.SnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SnapshotService {

    @Autowired
    private SnapshotRepository snapshotRepository;

    public List<Snapshot> getAllSnapshots(){return snapshotRepository.findAll();}

    public List<Snapshot> getAllSnapshotOrderByDESC(){return snapshotRepository.findAllByOrderByIdDesc();}

    public Optional<Snapshot> getSnapshotByID(Long id){return snapshotRepository.findById(id);}

    public Snapshot getNewestSnapshot(){return snapshotRepository.findTopByOrderByIdDesc();}

    public boolean ExistsByName(String name){return snapshotRepository.existsSnapshotByName(name);}

    public void createSnap(Snapshot snap){

        // Perist Snapshot
        snap.setDate(LocalDateTime.now());
        snapshotRepository.save(snap);
    }
}
