package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.FederalState;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = FederalState.class, idClass = Long.class)
public interface FederalStateRepository extends CrudRepository<FederalState, Long> {
    @Override
    List<FederalState> findAll();

    FederalState findByName(String name);
    FederalState findByShortcut(String shortcut);
}
