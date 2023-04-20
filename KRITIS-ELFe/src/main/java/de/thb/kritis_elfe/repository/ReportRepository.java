package de.thb.kritis_elfe.repository;

import de.thb.kritis_elfe.entity.Report;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;
import java.util.Optional;

@RepositoryDefinition(domainClass = Report.class, idClass = Long.class)
public interface ReportRepository extends CrudRepository<Report, Long> {

    List<Report> findAll();
    List<Report> findAllByOrderByIdDesc();
    Report findTopByOrderByIdDesc();
    boolean existsSnapshotByName(String name);
    Optional<Report> findById(Long id);
    Report findTopByIdLessThanOrderByIdDesc(long oldReportId);

}
