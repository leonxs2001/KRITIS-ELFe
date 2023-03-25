package de.thb.kritis_elfe.repository.questionnaire;

import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(domainClass = BranchQuestionnaire.class, idClass = Long.class)
public interface BranchQuestionnaireRepository extends CrudRepository<BranchQuestionnaire, Long> {

}
