package de.thb.webbaki.service.questionnaire;

import de.thb.webbaki.entity.Branch;
import de.thb.webbaki.entity.FederalState;
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
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Questionnaire getQuestionnaireForFederalState(FederalState federalState) {
        Questionnaire questionnaire = questionnaireRepository.findFirstByFederalStateOrderByIdDesc(federalState);

        if (questionnaire == null){
            // TODO change from usr to federal state
            questionnaire = Questionnaire.builder().federalState(federalState).build();

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
     */
    @Transactional
    public void saveQuestionnaireFromForm(Questionnaire questionnaire) {

        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
            for(UserScenario userScenario: branchQuestionnaire.getUserScenarios()){
                userScenarioService.updateUserScenarioValueAndCommentById(userScenario.getValue(), userScenario.getComment(), userScenario.getId());
            }
        }
    }

    @Transactional
    public Questionnaire saveQuestionnaireFromFiles(MultipartFile[] files, FederalState federalState, Model model){
        model.addAttribute("success", true);
        Questionnaire questionnaire = getQuestionnaireForFederalState(federalState);
        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(MultipartFile file: files){
            saveUserScenariosFromFile(questionnaire, file, model);
        }
        return questionnaire;
    }

    @Transactional
    protected void saveUserScenariosFromFile(Questionnaire questionnaire, MultipartFile file, Model model) {
        String text = getTextFromFile(file);
        //Branch is behind this combination
        String combination = "Branche    ";
        int indexOfAppearance = text.indexOf(combination);


        if(indexOfAppearance >= 0) {
            //slice everything before the Branch
            text = text.substring(indexOfAppearance).replaceAll("\r", "");
            indexOfAppearance = text.indexOf("\n");
            String branchNameFromFile = text.substring(combination.length(), indexOfAppearance);
            branchNameFromFile = branchNameFromFile.replaceAll("\\s", "").toLowerCase();
            boolean branchFound = false;
            //TODO EXCEPTION FOr bad input (after all indexOfAppearance >= 0)
            for (BranchQuestionnaire branchQuestionnaire : questionnaire.getBranchQuestionnaires()) {
                String branchName = branchQuestionnaire.getBranch().getName().replaceAll("\\s", "").toLowerCase();
                if (branchNameFromFile.contains(branchName)) {
                    branchFound = true;
                    text = text.substring(indexOfAppearance + 1);
                    text = text.replaceAll("\\s{2,3}", " ");

                    for (UserScenario userScenario : branchQuestionnaire.getUserScenarios()) {
                        String scenarioDescription = userScenario.getScenario().getDescription().replaceAll("\r", "").replaceAll("\\s{2,3}", " ");

                        scenarioDescription = sliceEmptyStartAndEnd(scenarioDescription);
                        indexOfAppearance = text.indexOf(scenarioDescription);
                        if (indexOfAppearance >= 0) {
                            int valueStartIndex = indexOfAppearance + scenarioDescription.length();
                            if(userScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {

                                String values = text.substring(valueStartIndex, valueStartIndex + 8).replaceAll("\\s", "");
                                valueStartIndex += 8;
                                for(int i = values.length() - 1; i >= 0 ; i--){
                                    if(values.charAt(i) == '☒'){
                                        userScenario.setValue((short)(i + 1));
                                        break;
                                    }
                                    if(i == 0){
                                        createModelListIfNotExistsAndInsertFilename(model, "noValuesGivenFileNames", file.getOriginalFilename());
                                    }
                                }
                            }
                            String slicedText = text.substring(valueStartIndex);
                            Pattern pattern = Pattern.compile("(\\d+. )|(keine / gar nicht( |\t)gering( |\t)erheblic)");
                            Matcher matcher = pattern.matcher(slicedText);
                            String comment;
                            if(matcher.find()){
                                comment = slicedText.substring(0, matcher.start());
                            }else{
                                comment = slicedText;
                            }
                            comment = sliceEmptyStartAndEnd(comment);
                            if(comment.contains("Bitte näher ausführen (z. B. personell, logistisch, materiell, gesetzgeberisch)")
                                    || comment.contains("Bitte immer ausfüllen, wenn nicht „grün“ ausgewählt")) {
                                comment = "";
                            }
                            userScenario.setComment(comment);

                        }else{
                            createModelListIfNotExistsAndInsertFilename(model, "scenarioNotMatchingFileNames", file.getOriginalFilename());
                        }

                        userScenarioService.saveUserScenario(userScenario);

                    }
                    break;
                }
            }

            if(!branchFound){
                createModelListIfNotExistsAndInsertFilename(model, "branchNotMatchingFileNames", file.getOriginalFilename());
            }
        }else{
            createModelListIfNotExistsAndInsertFilename(model, "branchStringMissingFileNames", file.getOriginalFilename());
        }

    }

    private void createModelListIfNotExistsAndInsertFilename(Model model, String attributeName, String filename){
        List<String> branchNotMatchingFileNames = (List<String>) model.getAttribute(attributeName);
        if(branchNotMatchingFileNames == null){
            branchNotMatchingFileNames = new ArrayList<>();
        }
        if(!branchNotMatchingFileNames.contains(filename)) {
            branchNotMatchingFileNames.add(filename);
        }
        model.addAttribute(attributeName, branchNotMatchingFileNames);
        model.addAttribute("success", false);
    }

    private String sliceEmptyStartAndEnd(String text){
        return text.replaceAll("(^\\s+)|(\\s+$)", "");
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