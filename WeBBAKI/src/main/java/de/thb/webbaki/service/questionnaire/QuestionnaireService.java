package de.thb.webbaki.service.questionnaire;

import de.thb.webbaki.entity.Branch;
import de.thb.webbaki.entity.questionnaire.BranchQuestionnaire;
import de.thb.webbaki.entity.questionnaire.Questionnaire;
import de.thb.webbaki.entity.Scenario;
import de.thb.webbaki.entity.User;
import de.thb.webbaki.entity.questionnaire.UserScenario;
import de.thb.webbaki.enums.ScenarioType;
import de.thb.webbaki.repository.questionnaire.QuestionnaireRepository;
import de.thb.webbaki.repository.UserRepository;
import de.thb.webbaki.service.BranchService;
import de.thb.webbaki.service.ScenarioService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
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

    public void save(Questionnaire questionnaire){questionnaireRepository.save(questionnaire);}
    public Questionnaire getQuestionnaire(long id) {return questionnaireRepository.findById(id);}
    public List<Questionnaire> getAllQuestionnaires(){
        return questionnaireRepository.findAll();
    }

    public Questionnaire getQuestionnaireForUser(User user) {
        Questionnaire questionnaire = questionnaireRepository.findFirstByFederalStateOrderByIdDesc(user.getFederalState());

        if (questionnaire == null){
            // TODO change from usr to federal state
            questionnaire = Questionnaire.builder().federalState(user.getFederalState()).build();

            List<Branch> branches = branchService.getAllBranches();
            List<Scenario> scenarios = scenarioService.getAllScenariosByActiveTrue();
            List<BranchQuestionnaire> branchQuestionnaires = new ArrayList<>();
            List<UserScenario> allUserScenarios = new ArrayList<>();

            for(Branch branch: branches){
                BranchQuestionnaire branchQuestionnaire = BranchQuestionnaire.builder().
                        questionnaire(questionnaire).
                        branch(branch).build();

                List<UserScenario> userScenarios = new ArrayList<>();

                for(Scenario scenario: scenarios){
                    UserScenario userScenario = UserScenario.builder()
                            .scenario(scenario)
                            .branchQuestionnaire(branchQuestionnaire)
                            .comment("").build();
                    userScenarios.add(userScenario);
                }
                allUserScenarios.addAll(userScenarios);
                branchQuestionnaire.setUserScenarios(userScenarios);
                branchQuestionnaires.add(branchQuestionnaire);

            }

            questionnaire.setBranchQuestionnaires(branchQuestionnaires);

            questionnaireRepository.save(questionnaire);
            branchQuestionnaireService.saveBranchQuestionnaires(branchQuestionnaires);
            userScenarioService.saveAllUserScenario(allUserScenarios);
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
        questionnaire.setFederalState(user.getFederalState());
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
    @Transactional
    public void saveQuestionnaireFromForm(Questionnaire questionnaire, User user) {

        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
            for(UserScenario userScenario: branchQuestionnaire.getUserScenarios()){
                userScenarioService.updateUserScenarioValueAndCommentById(userScenario.getValue(), userScenario.getComment(), userScenario.getId());
            }
        }
    }

    @Transactional
    public void saveQuestionnaireFromFiles(MultipartFile[] files, User user){
        Questionnaire questionnaire = getQuestionnaireForUser(user);
        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(MultipartFile file: files){
            saveUserScenariosFromFile(questionnaire, file);
        }
    }

    @Transactional
    protected void saveUserScenariosFromFile(Questionnaire questionnaire, MultipartFile file) {
        String text = getTextFromFile(file);
        //Branch is behind this combination
        String combination = "Branche    ";
        int indexOfAppearance = text.indexOf(combination);


        if(indexOfAppearance >= 0) {
            //slice everything before the Brand
            text = text.substring(indexOfAppearance).replaceAll("\r", "");
            indexOfAppearance = text.indexOf("\n");
            String branchNameFromFile = text.substring(combination.length(), indexOfAppearance);
            branchNameFromFile = branchNameFromFile.replaceAll("\\s", "").toLowerCase();

            //TODO EXCEPTION FOr bad input (after all indexOfAppearance >= 0)
            for (BranchQuestionnaire branchQuestionnaire : questionnaire.getBranchQuestionnaires()) {
                String branchName = branchQuestionnaire.getBranch().getName().replaceAll("\\s", "").toLowerCase();
                if (branchNameFromFile.contains(branchName)) {
                    text = text.substring(indexOfAppearance + 1);
                    text = text.replaceAll("\\s{2,3}", " ");

                    for (UserScenario userScenario : branchQuestionnaire.getUserScenarios()) {
                        boolean somethingChanged = false;
                        String scenarioDescription = userScenario.getScenario().getDescription().replaceAll("\r", "").replaceAll("\\s{2,3}", " ");
                        //cut last line break if exists
                        if(scenarioDescription.charAt(scenarioDescription.length() - 1) == '\n' || scenarioDescription.charAt(scenarioDescription.length() - 1) == ' '){
                            scenarioDescription = scenarioDescription.substring(0, scenarioDescription.length() - 1);
                        }
                        indexOfAppearance = text.indexOf(scenarioDescription);
                        if (indexOfAppearance >= 0) {
                            if(userScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {
                                int valueStartIndex = indexOfAppearance + scenarioDescription.length();
                                String values = text.substring(valueStartIndex, valueStartIndex + 8).replaceAll("\\s", "");
                                for(int i = values.length() - 1; i >= 0 ; i--){
                                    if(values.charAt(i) == '☒'){
                                        somethingChanged = true;
                                        userScenario.setValue((short)(i + 1));
                                        break;
                                    }
                                }
                            }else{
                                //TODO check text
                                System.out.println("alles andere später");
                            }
                        }

                        if(somethingChanged){
                            userScenarioService.saveUserScenario(userScenario);
                        }

                    }
                    break;
                }
            }
        }
    }

    private String getTextFromFile(MultipartFile file) {
        String extractedText = null;
        try{
            if(file.getContentType().equals("application/pdf")){
                PDDocument document = PDDocument.load(file.getInputStream());
                document.getClass();

                if (!document.isEncrypted()) {

                    PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                    stripper.setSortByPosition(true);

                    PDFTextStripper tStripper = new PDFTextStripper();

                    extractedText = tStripper.getText(document);

                }
                document.close();
            }else if(file.getContentType().equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")){
                XWPFDocument doc = new XWPFDocument(file.getInputStream());
                XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(doc);
                extractedText = xwpfWordExtractor.getText();
                doc.close();
            }

        } catch (IOException ioException){
            System.err.println(ioException);
        }

        return extractedText.replaceAll(Character.toString((char)160), " ");
    }

}