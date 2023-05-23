package de.thb.kritis_elfe.service.questionnaire;

import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.repository.questionnaire.QuestionnaireRepository;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.AccessDeniedException;
import de.thb.kritis_elfe.service.Exceptions.EntityDoesNotExistException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Builder
public class QuestionnaireService {
    private final QuestionnaireRepository questionnaireRepository;

    private final ScenarioService scenarioService;
    private final FilledScenarioService filledScenarioService;
    private final BranchService branchService;
    private final BranchQuestionnaireService branchQuestionnaireService;
    private final FederalStateService federalStateService;
    private final RessortService ressortService;
    private final UserService userService;
    private final RoleService roleService;
    private final DocumentService documentService;

    public void save(Questionnaire questionnaire){questionnaireRepository.save(questionnaire);}

    /**
     * Get the Questionnaire for the given federalstate.
     * Creates a new one if not exist.
     * @param federalState
     * @return
     */
    public Questionnaire getQuestionnaireForFederalState(FederalState federalState) {
        Questionnaire questionnaire = questionnaireRepository.findFirstByFederalStateOrderByIdDesc(federalState);

        if (questionnaire == null){
            questionnaire = Questionnaire.builder().federalState(federalState).build();

            List<Branch> branches = branchService.getAllBranches();
            fillQuestionnaireFromBranches(branches, questionnaire);
        }
        return questionnaire;
    }

    /**
     * Get the Questionnaire for the given ressort,
     * Checks if all branches are already included and include them if not.
     * Checks if all branches in the questionnaire are contained in the ressort and delete if not.
     * Creates a new one if not exist.
     * @param ressort
     * @return
     */
    public Questionnaire getQuestionnaireForRessort(Ressort ressort) {
        Questionnaire questionnaire = questionnaireRepository.findFirstByRessortOrderByIdDesc(ressort);
        List<Branch> branches;
        if (questionnaire == null){
            questionnaire = Questionnaire.builder().ressort(ressort).build();

             branches = ressort.getBranches();


        }else{
            branches = new ArrayList<>();
            //check if every branch of the ressort is present and add if not
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

            //check if every branch inside the questionnaire exist also in th ressort and delete if not
            questionnaire.getBranchQuestionnaires().removeIf( branchQuestionnaire -> !ressort.getBranches().contains(branchQuestionnaire.getBranch()));
        }
        fillQuestionnaireFromBranches(branches, questionnaire);

        return questionnaire;
    }

    /**
     * Fills the given Questionnaire with BranchQuestionnaires from given Branch and its FilledScenarios.
     * Saves all of them afterwards
     * @param branches
     * @param questionnaire
     */
    private void fillQuestionnaireFromBranches(List<Branch> branches, Questionnaire questionnaire) {
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
     * Save all the Questionnaire parts after getting it from form.
     * @param questionnaire
     * @param name
     * @param user
     * @throws EntityDoesNotExistException
     * @throws AccessDeniedException
     */
    @Transactional
    public void saveQuestionnaireFromForm(Questionnaire questionnaire, String name, User user) throws EntityDoesNotExistException, AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByName(name);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByName(name);
        }

        userService.checkAuthorizationOfUserForFederalStateOrRessort(user, federalState, ressort);

        if(federalState != null && !questionnaireRepository.existsByIdAndFederalState(questionnaire.getId(), federalState)) {
            throw new EntityDoesNotExistException("There is no questionnaire with the id " + questionnaire.getId() + " and the federal state " + federalState.getName() + ".");
        }else if(ressort != null && !questionnaireRepository.existsByIdAndRessort(questionnaire.getId(), ressort)) {
            throw new EntityDoesNotExistException("There is no questionnaire with the id " + questionnaire.getId() + " and the ressort " + ressort.getName() + ".");
        }else{
            questionnaireRepository.updateQuestionnaireDateAndUpdatedFromId(LocalDateTime.now(), true, questionnaire.getId());

            for (BranchQuestionnaire branchQuestionnaire : questionnaire.getBranchQuestionnaires()) {
                if(ressort == null || ressort.getBranches().contains(branchQuestionnaire.getBranch())){
                    for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                        filledScenarioService.updateFilledScenarioValueAndCommentById(filledScenario.getValue(), filledScenario.getComment(), filledScenario.getId());
                    }
                }
            }
        }
    }

    /**
     * Save all the Questionnaire parts after extracting them from the files
     * @param files
     * @param name
     * @param user
     * @param model
     * @return
     * @throws AccessDeniedException
     */
    @Transactional
    public Questionnaire saveQuestionnaireFromFiles(MultipartFile[] files, String name, User user, Model model) throws AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByName(name);
        Ressort ressort = null;
        if(federalState == null){
            ressort = ressortService.getRessortByName(name);
        }

        userService.checkAuthorizationOfUserForFederalStateOrRessort(user, federalState, ressort);

        model.addAttribute("success", true);
        Questionnaire questionnaire;
        if(federalState != null) {
            questionnaire = getQuestionnaireForFederalState(federalState);
        }else{
            questionnaire = getQuestionnaireForRessort(ressort);
        }
        questionnaireRepository.updateQuestionnaireDateFromId(LocalDateTime.now(), questionnaire.getId());//TODO Also updating if saved from files?

        for(MultipartFile file: files){
            saveFilledScenariosFromFile(questionnaire, file, ressort, model);
        }
        return questionnaire;
    }

    /**
     * Extract all the informations from the given File and save it.
     * @param questionnaire
     * @param file
     * @param ressort
     * @param model
     */
    @Transactional
    protected void saveFilledScenariosFromFile(Questionnaire questionnaire, MultipartFile file, Ressort ressort, Model model) {//TODO mache übersichtlicher
        String text = documentService.getTextFromFile(file);
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
                boolean branchFound = false;

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

                                //slice empty start and end
                                scenarioDescription = scenarioDescription.replaceAll("(^\\s+)|(\\s+$)", "");
                                scenarioDescription = scenarioDescription
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
                                        //slice empty start and end
                                        String comment = scenarioFilling.replaceAll("(^\\s+)|(\\s+$)", "");

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

    /**
     * Creates a new List inside the Model with the given Name if not exist.
     * And add the given filename to the list in every case.
     * @param model
     * @param attributeName
     * @param filename
     */
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

    /**
     * Go through all federalStates and ressorts copy all their questionnaires and assign the old ones to the report.
     * @param report
     */
    public void persistQuestionnairesForReport(Report report){
        List<Questionnaire> questionnaires = new ArrayList<>();
        for(FederalState federalState: federalStateService.getAllFederalStates()){
            Questionnaire questionnaire = getQuestionnaireForFederalState(federalState);
            questionnaire.setReport(report);
            questionnaires.add(questionnaire);

            cloneQuestionnaire(questionnaire);
        }

        for(Ressort ressort: ressortService.getAllRessorts()){
            Questionnaire questionnaire = getQuestionnaireForRessort(ressort);
            questionnaire.setReport(report);
            questionnaires.add(questionnaire);

            cloneQuestionnaire(questionnaire);
        }

        questionnaireRepository.saveAll(questionnaires);
    }

    /**
     * Copies and saves all parts of the questionnaire.
     * @param questionnaire
     */
    private void cloneQuestionnaire(Questionnaire questionnaire){
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

    /**
     * Returns all FederalStates which didn't have filled all Scenarios for every Branch.
     * @return
     */
    public List<FederalState> getFederalStatesWithNotUpdatedQuestionnaire(){
        List<FederalState> federalStates = new ArrayList<>();
        for(FederalState federalState: federalStateService.getAllFederalStates()){
            if(!getQuestionnaireForFederalState(federalState).isUpdated()){
                federalStates.add(federalState);
            }
        }
        return  federalStates;
    }

    /**
     * Returns all Ressorts which didn't have filled all Scenarios for every Branch.
     * @return
     */
    public List<Ressort> getRessortsWithNotUpdatedQuestionnaire(){
        List<Ressort> ressorts = new ArrayList<>();
        for(Ressort ressort: ressortService.getAllRessorts()){
            if(!getQuestionnaireForRessort(ressort).isUpdated()){
                ressorts.add(ressort);
            }
        }

        return ressorts;
    }

    /**
     * Get the Questionnaire for the Ressort or FederalState with the given name.
     * if the user is authorized to do that
     * @param name
     * @return Questionnaire for the FederalState or Ressort with the name
     */
    public Questionnaire getQuestionnaireFromCreatorsName(String name, User user) throws EntityDoesNotExistException, AccessDeniedException {
        FederalState federalState = federalStateService.getFederalStateByName(name);
        Ressort ressort = null;

        if (federalState == null) {
            ressort = ressortService.getRessortByName(name);
        }

        userService.checkAuthorizationOfUserForFederalStateOrRessort(user, federalState, ressort);

        if (federalState != null) {
            return getQuestionnaireForFederalState(federalState);
        } else if (ressort != null) {
            return getQuestionnaireForRessort(ressort);
        } else {
            throw new EntityDoesNotExistException("The Ressort or Federal State does not exist.");
        }
    }

}