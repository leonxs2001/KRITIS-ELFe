package de.thb.kritis_elfe.service.questionnaire;

import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.repository.questionnaire.QuestionnaireRepository;
import de.thb.kritis_elfe.repository.UserRepository;
import de.thb.kritis_elfe.service.BranchService;
import de.thb.kritis_elfe.service.Exceptions.AccessDeniedException;
import de.thb.kritis_elfe.service.Exceptions.EntityDoesNotExistException;
import de.thb.kritis_elfe.service.FederalStateService;
import de.thb.kritis_elfe.service.RessortService;
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
    private final FederalStateService federalStateService;
    private final RessortService ressortService;

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
            fillNewQuestionnaireFromBranches(branches, questionnaire);
        }

        return questionnaire;
    }

    public Questionnaire getQuestionnaireForRessort(Ressort ressort) {
        Questionnaire questionnaire = questionnaireRepository.findFirstByRessortOrderByIdDesc(ressort);
        List<Branch> branches;
        if (questionnaire == null){
            // TODO change from usr to federal state
            questionnaire = Questionnaire.builder().ressort(ressort).build();

             branches = ressort.getBranches();


        }else{//TODO delte old branches
            branches = new ArrayList<>();
            //check if every ressort is present
            for(Branch branch: ressort.getBranches()){
                boolean containsBranch = false;
                for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
                    if(branchQuestionnaire.getBranch().equals(branch)){
                        containsBranch = true;
                        break;
                    }
                }

                if(!containsBranch){
                    branches.add(branch);
                }

            }

        }

        fillNewQuestionnaireFromBranches(branches, questionnaire);

        return questionnaire;
    }

    private void fillNewQuestionnaireFromBranches(List<Branch> branches, Questionnaire questionnaire) {
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
        List<BranchQuestionnaire> newBranchQuestionnaires = questionnaire.getBranchQuestionnaires();
        if(newBranchQuestionnaires == null){
            newBranchQuestionnaires = new ArrayList<>();
        }

        newBranchQuestionnaires.addAll(branchQuestionnaires);
        questionnaire.setBranchQuestionnaires(newBranchQuestionnaires);
        questionnaire.setDate(LocalDateTime.now());
        questionnaireRepository.save(questionnaire);
        branchQuestionnaireService.saveBranchQuestionnaires(branchQuestionnaires);
        filledScenarioService.saveAllFilledScenarios(allFilledScenarios);
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
    public void saveQuestionnaireFromForm(Questionnaire questionnaire, FederalState federalState, Ressort ressort) throws EntityDoesNotExistException {

        if(federalState != null && !questionnaireRepository.existsByIdAndFederalState(questionnaire.getId(), federalState)) {
            throw new EntityDoesNotExistException("There is no questionnaire with the id " + questionnaire.getId() + " and the federal state " + federalState.getName() + ".");
        }else if(ressort != null && !questionnaireRepository.existsByIdAndRessort(questionnaire.getId(), ressort)) {
            throw new EntityDoesNotExistException("There is no questionnaire with the id " + questionnaire.getId() + " and the ressort " + ressort.getName() + ".");
        }else{

            questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

            for (BranchQuestionnaire branchQuestionnaire : questionnaire.getBranchQuestionnaires()) {
                if(ressort == null || ressort.getBranches().contains(branchQuestionnaire.getBranch())){
                    for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                        filledScenarioService.updateFilledScenarioValueAndCommentById(filledScenario.getValue(), filledScenario.getComment(), filledScenario.getId());
                    }
                }
            }
        }
    }

    @Transactional
    public Questionnaire saveQuestionnaireFromFiles(MultipartFile[] files, FederalState federalState, Ressort ressort, Model model){
        model.addAttribute("success", true);
        Questionnaire questionnaire;
        if(federalState != null) {
            questionnaire = getQuestionnaireForFederalState(federalState);
        }else{
            questionnaire = getQuestionnaireForRessort(ressort);
        }
        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());

        for(MultipartFile file: files){
            saveFilledScenariosFromFile(questionnaire, file, ressort, model);
        }
        return questionnaire;
    }

    @Transactional
    protected void saveFilledScenariosFromFile(Questionnaire questionnaire, MultipartFile file, Ressort ressort, Model model) {
        String text = getTextFromFile(file);
        int indexOfAppearance = text.indexOf("Fachlage");
        if(indexOfAppearance < 0){
            indexOfAppearance = text.indexOf("Sektor ");
        }

        if(indexOfAppearance > -1) {
            text = text.substring(indexOfAppearance).replaceAll("\r", "");

            String regex = "\n\\d+\\. (\\P{Cn})*?((\n\\d+\\. )|$)";
            String newText = text;
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(newText);
            List<String> scenarioListFromText = new ArrayList<>();


            while (matcher.find()) {
                String scenarioText = matcher.group();

                if (matcher.end() != text.length() - 1) {
                    scenarioText = scenarioText.replaceAll("\n\\d+\\. $", "");
                    newText = newText.substring(matcher.start() + scenarioText.length());
                    matcher = pattern.matcher(newText);
                }
                scenarioListFromText.add(scenarioText);
            }

            //Branch is behind this combination
            String combination = "Branche";
            indexOfAppearance = text.indexOf(combination);


            if (indexOfAppearance >= 0) {
                //slice everything before the Branch
                text = text.substring(indexOfAppearance);
                indexOfAppearance = text.indexOf("\n");
                String branchNameFromFile = text.substring(combination.length(), indexOfAppearance);
                branchNameFromFile = branchNameFromFile.replaceAll("\\s", "").toLowerCase();
                boolean branchFound = false;//TODO ist auch noch false,wenn durch ressort nicht nutzbar siehe unten

                for (BranchQuestionnaire branchQuestionnaire : questionnaire.getBranchQuestionnaires()) {
                    if (ressort == null || ressort.getBranches().contains(branchQuestionnaire.getBranch())) {
                        String newBranchNameFromFile = branchNameFromFile;
                        String branchName = branchQuestionnaire.getBranch().getName().replaceAll("\\s", "").toLowerCase();

                        //delete sector from branch in file if its not part of the branch name
                        String sectorName = branchQuestionnaire.getBranch().getSector().getName().replaceAll("\\s", "").toLowerCase();
                        if(!branchName.contains(sectorName)) {
                            newBranchNameFromFile = newBranchNameFromFile.replace(sectorName, "");
                        }

                        if (newBranchNameFromFile.contains(branchName)) {
                            branchFound = true;
                            for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                                String scenarioDescription = filledScenario.getScenario().getDescription().replaceAll("\r", "").replaceAll("\\s{2,3}", " ");
                                scenarioDescription = sliceEmptyStartAndEnd(scenarioDescription)
                                        .replace("\\", "\\\\")
                                        .replaceAll("\\(", "\\\\(")
                                        .replaceAll("\\)", "\\\\)")
                                        .replaceAll("\\[", "\\\\[")
                                        .replaceAll("\\]", "\\\\]")
                                        .replaceAll("\\{", "\\\\{")
                                        .replaceAll("\\}", "\\\\}")
                                        .replaceAll("\\*", "\\\\*")
                                        .replaceAll("\\+", "\\\\+")
                                        .replaceAll("\\?", "\\\\?")
                                        .replaceAll("\\.","\\\\.")
                                        .replaceAll("\\$","\\\\$")
                                        .replaceAll("\\^", "\\\\^")
                                        .replaceAll("\\&", "\\\\&")
                                        .replaceAll("\\|", "\\\\|")
                                        .replaceAll("sie", "(sie|diese)")
                                        .replaceAll(",", ",?")
                                        .replaceAll("\\s", "\\\\s{1,3}");
                                boolean scenarioFound = false;
                                for (String scenarioFromText : scenarioListFromText) {
                                    pattern = Pattern.compile(scenarioDescription);
                                    matcher = pattern.matcher(scenarioFromText);
                                    if (matcher.find()) {
                                        scenarioFound = true;
                                        String scenarioFilling = scenarioFromText.substring(matcher.end());

                                        if (filledScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {
                                            pattern = Pattern.compile("((☒|☐|T|£)\\s*){1,4}");
                                            matcher = pattern.matcher(scenarioFilling);

                                            if(matcher.find()){
                                                String values = matcher.group().replaceAll("\\s", "");
                                                scenarioFilling = scenarioFilling.substring(matcher.end());

                                                for (int i = values.length() - 1; i >= 0; i--) {
                                                    char value = values.charAt(i);
                                                    if (value == '☒' || value == 'T') {
                                                        filledScenario.setValue((short) (i + 1 + 4 - values.length()));//to match also ones with only 3 values
                                                        break;
                                                    }
                                                    if (i == 0) {//not filled
                                                        createModelListIfNotExistsAndInsertFilename(model, "noValuesGivenFileNames", file.getOriginalFilename());
                                                    }
                                                }
                                            }else{
                                                createModelListIfNotExistsAndInsertFilename(model, "noValuesGivenFileNames", file.getOriginalFilename());
                                            }
                                        }
                                        String comment = sliceEmptyStartAndEnd(scenarioFilling);
                                        List<String> replaceables = new ArrayList<>();
                                        replaceables.add("\\s?keine\\s/\\s{1,2}gar\\s{1,2}nicht\\s{1,2}gering\\s{1,2}erheb(-\\s)?lic(\\s{0,3})h\\s{1,2}massiv\\sKonkretisierung\\s?");
                                        replaceables.add("\\s{0,3}Lageprognose\\s/\\slängerfristige\\sPerspektive\\s\\(bitte\\sZeithorizont\\sder\\sAussage\\sim\\sFreitextfeld\\sspezifizieren\\)\\s{0,3}");
                                        replaceables.add("\\s?!\\sVSA-Einstufung\\sauswählen\\s!\\s{0,3}");
                                        replaceables.add("\\s?Gemeinsames\\s{1,2}Kompetenzzentrum\\s{1,2}Bevölkerungsschutz\\s+Seite\\s\\d+\\svon\\s\\d+\\sStand:\\sGemeinsames\\sLagebild\\sBevölkerungsschutz\\s–\\sMeldevorlage\\s[A-Za-z] -\\sKRITIS\\s?");
                                        replaceables.add("\\s?keine\\s/\\s{1,2}gar\\snicht\\serheblic\\s?h\\s{1,2}kritisch\\s?/\\s{1,2}umfasse\\s?nd(\\s{1,2}Konkretisierung\\s{1,2}gering)?\\s?");
                                        replaceables.add("\\s?Bitte\\snäher\\sausführen\\s\\(z.\\sB.\\sorganisatorisch,\\spersonell,\\slogistisch,\\smateriell,\\sgesetzgeberisch\\)\\s?");
                                        replaceables.add("\\s?Bitte\\simmer\\sausfüllen,\\swenn\\snicht\\s„grün“\\sausgewählt(\\swurde\\.)?\\s?");
                                        replaceables.add("\\s?((Personenbezogene Einschränkungen bzw. Personalausfälle)" +
                                                "|(Technikeinschränkungen / technische Ausstattung)" +
                                                "|(Technikeinschränkungen / technische Ausstattung)" +
                                                "|(Gefährdung / Beeinträchtigung IT-Systeme \\(IT-Security\\))" +
                                                "|(Einschränkungen bei Betriebsmitteln)|(Organisatorische Einwirkungen / Einschränkungen)" +
                                                "|(\\(z. B. organisatorisch, personell, logistisch, materiell, gesetzgeberisch\\))"+
                                                "|(Sonstiges und äußere Einflüsse))\\s-\\s?" );
                                        for (String replaceable : replaceables) {
                                            comment = comment.replaceAll(replaceable, "");
                                        }
                                        if(comment.length() > 10000){
                                            comment = comment.substring(0,9999);
                                            createModelListIfNotExistsAndInsertFilename(model, "commentToLongFileNames", file.getOriginalFilename());
                                        }
                                        filledScenario.setComment(comment.replaceAll("\t","\n"));
                                        break;
                                    }
                                }
                                //scenario not found
                                if (!scenarioFound) {
                                    createModelListIfNotExistsAndInsertFilename(model, "scenarioNotMatchingFileNames", file.getOriginalFilename());
                                }

                                filledScenarioService.saveFilledScenario(filledScenario);

                            }
                            break;
                        }
                    } else {
                        createModelListIfNotExistsAndInsertFilename(model, "branchNotForThisRessort", file.getOriginalFilename());
                    }
                }

                if (!branchFound) {
                    createModelListIfNotExistsAndInsertFilename(model, "branchNotMatchingFileNames", file.getOriginalFilename());
                }
            } else {
                createModelListIfNotExistsAndInsertFilename(model, "branchStringMissingFileNames", file.getOriginalFilename());
            }
        }else{
            createModelListIfNotExistsAndInsertFilename(model, "sectorStringMissingFileNames", file.getOriginalFilename());
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

    public void persistQuestionnairesForReport(Report report){
        List<Questionnaire> questionnaires = new ArrayList<>();
        for(FederalState federalState: federalStateService.getAllFederalStates()){
            Questionnaire questionnaire = getQuestionnaireForFederalState(federalState);
            questionnaire.setReport(report);
            questionnaires.add(questionnaire);

            duplicateQuestionnaire(questionnaire);
        }

        for(Ressort ressort: ressortService.getAllRessorts()){
            Questionnaire questionnaire = getQuestionnaireForRessort(ressort);
            questionnaire.setReport(report);
            questionnaires.add(questionnaire);

            duplicateQuestionnaire(questionnaire);
        }

        questionnaireRepository.saveAll(questionnaires);
    }

    private void duplicateQuestionnaire(Questionnaire questionnaire){
        Questionnaire newQuestionnaire = Questionnaire.builder()
                .federalState(questionnaire.getFederalState())
                .ressort(questionnaire.getRessort())
                .date(questionnaire.getDate()).build();

        questionnaireRepository.save(newQuestionnaire);

        for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
            BranchQuestionnaire newBranchQuestionnaire = BranchQuestionnaire.builder()
                    .questionnaire(newQuestionnaire)
                    .branch(branchQuestionnaire.getBranch()).build();
            branchQuestionnaireService.saveBranchQuestionnaire(newBranchQuestionnaire);

            for(FilledScenario filledScenario: branchQuestionnaire.getFilledScenarios()){
                FilledScenario newFilledScenario = FilledScenario.builder()
                        .branchQuestionnaire(newBranchQuestionnaire)
                        .value(filledScenario.getValue())
                        .comment(filledScenario.getComment())
                        .scenario(filledScenario.getScenario()).build();

                filledScenarioService.saveFilledScenario(newFilledScenario);
            }
        }
    }

    public List<FederalState> getFederalStatesWithEmptyQuestionnaire(){
        List<FederalState> federalStates = new ArrayList<>();
        for(FederalState federalState: federalStateService.getAllFederalStates()){
            if(!isQuestionnaireFullyFilled(getQuestionnaireForFederalState(federalState))){
                federalStates.add(federalState);
            }
        }
        return  federalStates;
    }

    public List<Ressort> getRessortsWithEmptyQuestionnaire(){
        List<Ressort> ressorts = new ArrayList<>();
        for(Ressort ressort: ressortService.getAllRessorts()){
            if(!isQuestionnaireFullyFilled(getQuestionnaireForRessort(ressort))){
                ressorts.add(ressort);
            }
        }

        return ressorts;
    }

    private boolean isQuestionnaireFullyFilled(Questionnaire questionnaire){
        for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
            for(FilledScenario filledScenario: branchQuestionnaire.getFilledScenarios()){
                if(filledScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL && filledScenario.getValue() <= 0){
                    return false;
                }
            }
        }

        return true;
    }

}