package de.thb.webbaki.repository;

import de.thb.webbaki.entity.FederalState;
import de.thb.webbaki.entity.Ressort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = Ressort.class, idClass = Long.class)
public interface RessortRepository extends CrudRepository<Ressort, Long> {
    @Override
    List<Ressort> findAll();

    Ressort findByName(String name);
}
