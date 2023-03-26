package de.thb.kritis_elfe.repository.questionnaire;

import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;


@RepositoryDefinition(domainClass = FilledScenario.class, idClass = Long.class)
public interface FilledScenarioRepository extends CrudRepository<FilledScenario, Long> {
    @Modifying
    @Query("update FilledScenario us set us.value = ?1, us.comment = ?2 where us.id = ?3")
    void updateFilledScenarioValueAndCommentDateFromId(short value, String comment, long id);
}
