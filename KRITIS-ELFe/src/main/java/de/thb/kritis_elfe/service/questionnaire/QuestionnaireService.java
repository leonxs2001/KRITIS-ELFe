package de.thb.kritis_elfe.service.questionnaire;

import de.thb.kritis_elfe.entity.Branch;
import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.entity.Scenario;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.repository.questionnaire.QuestionnaireRepository;
import de.thb.kritis_elfe.repository.UserRepository;
import de.thb.kritis_elfe.service.BranchService;
import de.thb.kritis_elfe.service.ScenarioService;
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
    private final FilledScenarioService filledScenarioService;
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
            List<FilledScenario> allFilledScenarios = new ArrayList<>();

            for(Branch branch: branches){
                BranchQuestionnaire branchQuestionnaire = BranchQuestionnaire.builder().
                        questionnaire(questionnaire).
                        branch(branch).build();

                List<FilledScenario> filledScenarios = new ArrayList<>();

                for(Scenario scenario: scenarios){
                    FilledScenario filledScenario = FilledScenario.builder()
                            .scenario(scenario)
                            .branchQuestionnaire(branchQuestionnaire)
                            .comment("").build();
                    filledScenarios.add(filledScenario);
                }
                allFilledScenarios.addAll(filledScenarios);
                branchQuestionnaire.setFilledScenarios(filledScenarios);
                branchQuestionnaires.add(branchQuestionnaire);

            }

            questionnaire.setBranchQuestionnaires(branchQuestionnaires);

            questionnaireRepository.save(questionnaire);
            branchQuestionnaireService.saveBranchQuestionnaires(branchQuestionnaires);
            filledScenarioService.saveAllFilledScenarios(allFilledScenarios);
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
        List<FilledScenario> filledScenarios = new LinkedList<>();
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
        filledScenarioService.saveAllFilledScenarios(filledScenarios);

        return questionnaire;
    }

    /**
     * Checks if all active scenarios have a representation in the questionnaires FilledScenarios
     * --> add an empty FilledScenario from this Scenario if not.
     * delete FilledScenarios with an inactive Scenario from this questionnaire
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
     * Create a new Questionnaire with the FilledScenarios from inside the form and save it
     */
    @Transactional
    public void saveQuestionnaireFromForm(Questionnaire questionnaire) {

        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
            for(FilledScenario filledScenario: branchQuestionnaire.getFilledScenarios()){
                filledScenarioService.updateFilledScenarioValueAndCommentById(filledScenario.getValue(), filledScenario.getComment(), filledScenario.getId());
            }
        }
    }

    @Transactional
    public Questionnaire saveQuestionnaireFromFiles(MultipartFile[] files, FederalState federalState, Model model){
        model.addAttribute("success", true);
        Questionnaire questionnaire = getQuestionnaireForFederalState(federalState);
        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(MultipartFile file: files){
            saveFilledScenariosFromFile(questionnaire, file, model);
        }
        return questionnaire;
    }

    @Transactional
    protected void saveFilledScenariosFromFile(Questionnaire questionnaire, MultipartFile file, Model model) {
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

                    for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                        String scenarioDescription = filledScenario.getScenario().getDescription().replaceAll("\r", "").replaceAll("\\s{2,3}", " ");

                        scenarioDescription = sliceEmptyStartAndEnd(scenarioDescription);
                        indexOfAppearance = text.indexOf(scenarioDescription);
                        if (indexOfAppearance >= 0) {
                            int valueStartIndex = indexOfAppearance + scenarioDescription.length();
                            if(filledScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {

                                String values = text.substring(valueStartIndex, valueStartIndex + 8).replaceAll("\\s", "");
                                valueStartIndex += 8;
                                for(int i = values.length() - 1; i >= 0 ; i--){
                                    if(values.charAt(i) == '☒'){
                                        filledScenario.setValue((short)(i + 1));
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
                            filledScenario.setComment(comment);

                        }else{
                            createModelListIfNotExistsAndInsertFilename(model, "scenarioNotMatchingFileNames", file.getOriginalFilename());
                        }

                        filledScenarioService.saveFilledScenario(filledScenario);

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