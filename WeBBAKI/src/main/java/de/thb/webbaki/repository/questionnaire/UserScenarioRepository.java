package de.thb.webbaki.repository.questionnaire;

import de.thb.webbaki.entity.questionnaire.UserScenario;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.time.LocalDateTime;


@RepositoryDefinition(domainClass = UserScenario.class, idClass = Long.class)
public interface UserScenarioRepository extends CrudRepository<UserScenario, Long> {
    @Modifying
    @Query("update UserScenario us set us.value = ?1, us.comment = ?2 where us.id = ?3")
    void updateUserScenarioValueAndCommentDateFromId(short value, String comment, long id);
}
