package de.thb.webbaki.repository;

import de.thb.webbaki.entity.Scenario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;
import java.util.Optional;

@RepositoryDefinition(domainClass = Scenario.class, idClass = Long.class)
public interface ScenarioRepository extends CrudRepository<Scenario, Long> {

    Scenario findById(long id);

    Scenario findByDescriptionAndActive(String description, boolean active);

    List<Scenario> findAll();

    List<Scenario> findByActive(boolean active);


}