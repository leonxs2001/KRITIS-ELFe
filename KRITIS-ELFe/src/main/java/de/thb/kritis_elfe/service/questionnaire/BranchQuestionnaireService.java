package de.thb.kritis_elfe.service.questionnaire;

import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import de.thb.kritis_elfe.repository.questionnaire.BranchQuestionnaireRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Builder
public class BranchQuestionnaireService {
    private final BranchQuestionnaireRepository branchQuestionnaireRepository;

    public BranchQuestionnaire saveBranchQuestionnaire(BranchQuestionnaire branchQuestionnaire){
        return branchQuestionnaireRepository.save(branchQuestionnaire);
    }

    public void saveBranchQuestionnaires(List<BranchQuestionnaire> branchQuestionnaires){
        branchQuestionnaireRepository.saveAll(branchQuestionnaires);
    }
}