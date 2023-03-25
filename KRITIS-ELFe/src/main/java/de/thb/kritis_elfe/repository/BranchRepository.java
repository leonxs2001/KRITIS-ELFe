package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.Branch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = Branch.class, idClass = Long.class)
public interface BranchRepository extends CrudRepository<Branch, Long> {
    @Override
    List<Branch> findAll();
    Branch findByName(String name);
    Branch findById(long id);
}
