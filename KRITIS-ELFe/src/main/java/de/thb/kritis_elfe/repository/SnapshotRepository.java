package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.Snapshot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;
import java.util.Optional;

@RepositoryDefinition(domainClass = Snapshot.class, idClass = Long.class)
public interface SnapshotRepository extends CrudRepository<Snapshot, Long> {

    List<Snapshot> findAll();
    List<Snapshot> findAllByOrderByIdDesc();
    Snapshot findTopByOrderByIdDesc();
    boolean existsSnapshotByName(String name);
    Optional<Snapshot> findById(Long id);

}
