package de.thb.webbaki.repository.questionnaire;

import de.thb.webbaki.entity.FederalState;
import de.thb.webbaki.entity.questionnaire.Questionnaire;
import de.thb.webbaki.entity.User;
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
    List<Questionnaire> findAll();

    @Modifying
    @Query("update Questionnaire quest set quest.date = ?1 where quest.id = ?2")
    void updateQuestionnaireDateFromId(LocalDateTime localDateTime, long id);
}
