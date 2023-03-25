package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.Sector;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = Sector.class, idClass = Long.class)
public interface SectorRepository extends CrudRepository<Sector, Long> {
    Sector findByBranches_name(String name);
    Sector findByName(String name);
    @Override
    List<Sector> findAll();
}
