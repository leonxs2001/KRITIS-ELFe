package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.Scenario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = Scenario.class, idClass = Long.class)
public interface ScenarioRepository extends CrudRepository<Scenario, Long> {

    Scenario findById(long id);

    Scenario findByDescriptionAndActive(String description, boolean active);

    List<Scenario> findAll();

    List<Scenario> findByActive(boolean active);


}