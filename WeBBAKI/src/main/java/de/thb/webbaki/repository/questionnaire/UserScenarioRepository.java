package de.thb.webbaki.repository.questionnaire;

import de.thb.webbaki.entity.questionnaire.UserScenario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;


@RepositoryDefinition(domainClass = UserScenario.class, idClass = Long.class)
public interface UserScenarioRepository extends CrudRepository<UserScenario, Long> {
}
