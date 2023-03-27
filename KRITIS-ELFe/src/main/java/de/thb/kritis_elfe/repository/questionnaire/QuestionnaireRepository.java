package de.thb.kritis_elfe.repository.questionnaire;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.time.LocalDateTime;
import java.util.List;

@RepositoryDefinition(domainClass = Questionnaire.class, idClass = Long.class)
public interface QuestionnaireRepository extends CrudRepository<Questionnaire, Long> {

    Questionnaire findById(long id);
    Questionnaire findAllByFederalState(FederalState federalState);
    Questionnaire findFirstByFederalStateOrderByIdDesc(FederalState federalState);
    Questionnaire findFirstByRessortOrderByIdDesc(Ressort ressort);
    boolean existsByIdAndFederalState(long id, FederalState federalState);
    boolean existsByIdAndRessort(long id, Ressort ressort);
    List<Questionnaire> findAll();

    @Modifying
    @Query("update Questionnaire quest set quest.date = ?1 where quest.id = ?2")
    void updateQuestionnaireDateFromId(LocalDateTime localDateTime, long id);
}
