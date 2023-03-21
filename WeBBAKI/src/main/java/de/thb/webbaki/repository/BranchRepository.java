package de.thb.webbaki.repository;

import de.thb.webbaki.entity.Branch;
import de.thb.webbaki.entity.Sector;
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
