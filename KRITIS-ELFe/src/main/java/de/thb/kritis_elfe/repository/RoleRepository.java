package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.Optional;

@RepositoryDefinition(domainClass = Role.class, idClass = Long.class)
public interface RoleRepository extends CrudRepository<Role, Long> {

    Role findByName(String name);
    Optional<Role> findById(Long id);
}
