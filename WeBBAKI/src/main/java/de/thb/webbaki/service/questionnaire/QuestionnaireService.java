package de.thb.webbaki.service.questionnaire;

import de.thb.webbaki.entity.Branch;
import de.thb.webbaki.entity.questionnaire.BranchQuestionnaire;
import de.thb.webbaki.entity.questionnaire.Questionnaire;
import de.thb.webbaki.entity.Scenario;
import de.thb.webbaki.entity.User;
import de.thb.webbaki.entity.questionnaire.UserScenario;
import de.thb.webbaki.repository.questionnaire.QuestionnaireRepository;
import de.thb.webbaki.repository.UserRepository;
import de.thb.webbaki.service.BranchService;
import de.thb.webbaki.service.ScenarioService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
@Builder
public class QuestionnaireService {
    private final QuestionnaireRepository questionnaireRepository;
    private final UserRepository userRepository;

    private final ScenarioService scenarioService;
    private final UserScenarioService userScenarioService;
    private final BranchService branchService;
    private final BranchQuestionnaireService branchQuestionnaireService;

    public boolean existsByUserId(long id){return questionnaireRepository.existsByUser_id(id);}
    public boolean existsByIdAndUserId(long questId, long userId){return questionnaireRepository.existsByIdAndUser_Id(questId, userId);}
    public void save(Questionnaire questionnaire){questionnaireRepository.save(questionnaire);}
    public Questionnaire getQuestionnaire(long id) {return questionnaireRepository.findById(id);}
    public Questionnaire getNewestQuestionnaireByUserId(long id) {return questionnaireRepository.findFirstByUser_IdOrderByIdDesc(id);}
    public List<Questionnaire> getAllQuestByUser(long id) {return questionnaireRepository.findAllByUser(userRepository.findById(id).get());}
    public List<Questionnaire> getAllQuestionnaires(){
        return questionnaireRepository.findAll();
    }

    public Questionnaire getQuestionnaireForUser(User user) {
        Questionnaire questionnaire = questionnaireRepository.findFirstByUser_UsernameOrderByIdDesc(user.getUsername());

        if (questionnaire == null){
            // TODO change from usr to federal state
            questionnaire = Questionnaire.builder().user(user).build();

            List<Branch> branches = branchService.getAllBranches();
            List<Scenario> scenarios = scenarioService.getAllScenariosByActiveTrue();
            List<BranchQuestionnaire> branchQuestionnaires = new ArrayList<>();

            for(Branch branch: branches){
                BranchQuestionnaire branchQuestionnaire = BranchQuestionnaire.builder().
                        questionnaire(questionnaire).
                        branch(branch).build();

                List<UserScenario> userScenarios = new ArrayList<>();

                for(Scenario scenario: scenarios){
                    UserScenario userScenario = UserScenario.builder()
                            .scenario(scenario)
                            .comment("").build();
                    userScenarios.add(userScenario);
                }

                branchQuestionnaire.setUserScenarios(userScenarios);
                branchQuestionnaires.add(branchQuestionnaire);

            }

            questionnaire.setBranchQuestionnaires(branchQuestionnaires);
        }

        return questionnaire;
    }
    /**
     * @param user
     * @return new questionnaire for the given user.
     * Creates and save the new Questionnaire
     */
    public Questionnaire createQuestionnaireForUser(User user){
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setDate(LocalDateTime.now());
        questionnaire.setUser(user);
        questionnaireRepository.save(questionnaire);

        //create a UserScenario for every active Scenario
        List<Scenario> scenarios = scenarioService.getAllScenarios();
        List<UserScenario> userScenarios = new LinkedList<>();
        for(Scenario scenario : scenarios){
            if(scenario.isActive()) {
                /*UserScenario userScenario = UserScenario.builder().smallComment("")
                        .scenario(scenario)
                        .questionnaire(questionnaire)
                        .impact(-1)
                        .probability(-1).build();
                */
                //userScenarios.add(userScenario);
            }
        }
        userScenarioService.saveAllUserScenario(userScenarios);

        return questionnaire;
    }

    /**
     * Checks if all active scenarios have a representation in the questionnaires UserScenarios
     * --> add an empty UserScenario from this Scenario if not.
     * delete UserScenarios with an inactive Scenario from this questionnaire
     * @param questionnaire
     */
    /*public void checkIfMatchingWithActiveScenariosFromDB(Questionnaire questionnaire){
        List<Scenario> activeScenarios = scenarioService.getAllScenariosByActiveTrue();
        //create a copy of the UserScenarioList. Because we need to delete items from the list inside the Questionnaire.
        List<UserScenario> userScenarios = new ArrayList<>(questionnaire.getUserScenarios());

        //try to remove every UserScenario of the Questionnaire from the activeScenario-list
        //and remove it from the questionnaire List, if its not part of the List
        for(UserScenario userScenario : userScenarios){
            //try to remove the Scenario
            if(!activeScenarios.remove(userScenario.getScenario())){
                //delete the UserScenario from the Questionnaire, if it is not from an active Scenario (not in active list)
                questionnaire.getUserScenarios().remove(userScenario);
            }
        }

        //all scenarios which were not deleted have to be created as new UserScenario for the Questionnaire
        for(Scenario scenario : activeScenarios){
            if (!userScenarioService.existsUerScenarioByScenarioIdAndQuestionnaireId(scenario.getId(), questionnaire.getId())) {
                UserScenario userScenario = UserScenario.builder().smallComment("")
                        .scenario(scenario)
                        .questionnaire(questionnaire)
                        .impact(-1)
                        .probability(-1).build();
                questionnaire.getUserScenarios().add(userScenario);
            }
        }
    }*/

    /**
     * Create a new Questionnaire with the UserScenarios from inside the form and save it
     * @param user
     */
    public void saveQuestionnaireFromForm(Questionnaire questionnaire, User user) {

        questionnaire.setDate(LocalDateTime.now());
        questionnaire.setUser(user);

        questionnaireRepository.save(questionnaire);

        for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
            branchQuestionnaire.setQuestionnaire(questionnaire);
            branchQuestionnaire.setBranch(branchService.getBranchById(branchQuestionnaire.getBranch().getId()));
            branchQuestionnaireService.createBranchQuestionnaire(branchQuestionnaire);

            for(UserScenario userScenario: branchQuestionnaire.getUserScenarios()){
                userScenario.setBranchQuestionnaire(branchQuestionnaire);
                userScenario.setScenario(scenarioService.getScenarioById(userScenario.getScenario().getId()));
                userScenarioService.saveUserScenario(userScenario);
            }
        }




    }

}