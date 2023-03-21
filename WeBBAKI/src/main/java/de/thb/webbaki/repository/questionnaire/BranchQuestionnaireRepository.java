package de.thb.webbaki.repository.questionnaire;

import de.thb.webbaki.entity.User;
import de.thb.webbaki.entity.questionnaire.BranchQuestionnaire;
import de.thb.webbaki.entity.questionnaire.Questionnaire;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

import java.util.List;

@RepositoryDefinition(domainClass = BranchQuestionnaire.class, idClass = Long.class)
public interface BranchQuestionnaireRepository extends CrudRepository<BranchQuestionnaire, Long> {

}
