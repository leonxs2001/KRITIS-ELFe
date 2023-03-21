package de.thb.webbaki.service.questionnaire;

import de.thb.webbaki.entity.questionnaire.BranchQuestionnaire;
import de.thb.webbaki.repository.questionnaire.BranchQuestionnaireRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Builder
public class BranchQuestionnaireService {
    private final BranchQuestionnaireRepository branchQuestionnaireRepository;

    public BranchQuestionnaire createBranchQuestionnaire(BranchQuestionnaire branchQuestionnaire){
        return branchQuestionnaireRepository.save(branchQuestionnaire);
    }

    public void createBranchQuestionnaires(List<BranchQuestionnaire> branchQuestionnaires){
        branchQuestionnaireRepository.saveAll(branchQuestionnaires);
    }
}