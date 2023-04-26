package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.enums.ValueChangedType;
import de.thb.kritis_elfe.repository.ReportRepository;
import de.thb.kritis_elfe.service.helper.report.*;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    QuestionnaireService questionnaireService;

    public List<Report> getAllReports(){return reportRepository.findAll();}

    public List<Report> getAllReportsOrderByDESC(){return reportRepository.findAllByOrderByIdDesc();}

    public Report getReportById(Long id){return reportRepository.findById(id).get();}

    public Report getNewestReport(){return reportRepository.findTopByOrderByIdDesc();}

    public boolean ExistsByName(String name){return reportRepository.existsSnapshotByName(name);}

    public void createReport(Report report){

        // Perist Snapshot
        report.setDate(LocalDateTime.now());
        reportRepository.save(report);

        questionnaireService.persistQuestionnairesForReport(report);

    }


    public SectorBranchReportValueAccessor createSectorBranchReportValueAccessor(Report report, Sector sector){
        if(report == null){
            return null;
        }

        HashMap<Branch, HashMap<FederalState, CommentReportValue>> federalStateBranchCommentReportValueHashMap = new HashMap<>();
        HashMap<Branch, RessortCommentsReportValue> branchRessortCommentsReportValue = new HashMap<>();

        fillReportValuesAndHashMapsFromReportForSector(report, sector, branchRessortCommentsReportValue,
                federalStateBranchCommentReportValueHashMap);

        Report oldReport = reportRepository.findTopByIdLessThanOrderByIdDesc(report.getId());

        HashMap<Branch, HashMap<FederalState, CommentReportValue>> oldFederalStateBranchCommentReportValueHashMap = new HashMap<>();
        HashMap<Branch, RessortCommentsReportValue> oldBranchRessortCommentsReportValue = new HashMap<>();

        if(oldReport != null){
            fillReportValuesAndHashMapsFromReportForSector(oldReport, sector, oldBranchRessortCommentsReportValue,
                    oldFederalStateBranchCommentReportValueHashMap);
        }

        //add all change types
        federalStateBranchCommentReportValueHashMap.forEach((branch, federalStateCommentReportValueHashMap) -> {
            federalStateCommentReportValueHashMap.forEach((federalState, commentReportValue) -> {
                CommentReportValue oldCommentReportValue = null;
                if(oldReport != null) {
                    oldCommentReportValue = oldFederalStateBranchCommentReportValueHashMap.get(branch).get(federalState);
                }

                setValueChangedTypeByOldValue(commentReportValue, oldCommentReportValue);
            });
        });

        branchRessortCommentsReportValue.forEach((branch, ressortCommentsReportValue) -> {
            setValueChangedTypeByOldValue(ressortCommentsReportValue, oldBranchRessortCommentsReportValue.get(branch));
        });

        return new SectorBranchReportValueAccessor(federalStateBranchCommentReportValueHashMap, branchRessortCommentsReportValue);
    }

    //TODO many code duplicates make it simple
    private void fillReportValuesAndHashMapsFromReportForSector(Report report, Sector sector, HashMap<Branch, RessortCommentsReportValue> branchRessortCommentsReportValue,
                                                       HashMap<Branch, HashMap<FederalState, CommentReportValue>> federalStateBranchCommentReportValueHashMap){
        for(Questionnaire questionnaire: report.getQuestionnaires()){
            for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){

                //only take branchQuestionnaires with branch inside the sector
                if(sector.getBranches().contains(branchQuestionnaire.getBranch())) {
                    if (questionnaire.getFederalState() != null) {
                        HashMap<FederalState, CommentReportValue> federalStateCommentReportValueHashMap = federalStateBranchCommentReportValueHashMap.get(branchQuestionnaire.getBranch());
                        if(federalStateCommentReportValueHashMap == null){
                            federalStateCommentReportValueHashMap = new HashMap<>();
                            federalStateBranchCommentReportValueHashMap.put(branchQuestionnaire.getBranch(), federalStateCommentReportValueHashMap);
                        }

                        CommentReportValue commentReportValue = federalStateCommentReportValueHashMap.get(questionnaire.getFederalState());
                        if(commentReportValue == null){
                            commentReportValue = new CommentReportValue();
                            federalStateCommentReportValueHashMap.put(questionnaire.getFederalState(), commentReportValue);
                        }

                        for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                            if(filledScenario.getValue() > 1 && !filledScenario.getComment().equals("")){//TODO many code duplicates make it simple
                                commentReportValue.getComments().add(filledScenario.getComment());
                            }
                            if (filledScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {
                                if (commentReportValue.getValue() < filledScenario.getValue()) {
                                    commentReportValue.setValue(filledScenario.getValue());
                                }
                            }
                        }
                    } else {//is Ressort
                        RessortCommentsReportValue ressortCommentsReportValue = branchRessortCommentsReportValue.get(branchQuestionnaire.getBranch());
                        if(ressortCommentsReportValue == null){
                            ressortCommentsReportValue = new RessortCommentsReportValue();
                            branchRessortCommentsReportValue.put(branchQuestionnaire.getBranch(), ressortCommentsReportValue);
                        }

                        List<String> comments = ressortCommentsReportValue.getComments(questionnaire.getRessort());
                        if(comments == null){
                            comments = new ArrayList<>();
                            ressortCommentsReportValue.getRessortComments().put(questionnaire.getRessort(), comments);
                        }

                        for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                            if(filledScenario.getValue() > 1 && !filledScenario.getComment().equals("")){
                                comments.add(filledScenario.getComment());
                            }

                            if (filledScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {
                                if (ressortCommentsReportValue.getValue() < filledScenario.getValue()) {
                                    ressortCommentsReportValue.setValue(filledScenario.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public SectorReportValueAccessor createSectorReportValueAccessor(Report report){

        if(report == null){
            return null;
        }

        HashMap<Sector, HashMap<FederalState, ReportValue>> federalStateSectorReportValueHashMap = new HashMap<>();
        HashMap<Sector, ReportValue> ressortSectorReportValueHashMap = new HashMap<>();

        fillSectorReportValueHashMapsFromReport(report, federalStateSectorReportValueHashMap, ressortSectorReportValueHashMap);

        HashMap<Sector, HashMap<FederalState, ReportValue>> oldFederalStateSectorReportValuesHashMap = new HashMap<>();
        HashMap<Sector, ReportValue> oldRessortSectorReportValueHashMap = new HashMap<>();
        Report oldReport = reportRepository.findTopByIdLessThanOrderByIdDesc(report.getId());

        if(oldReport != null) {
            fillSectorReportValueHashMapsFromReport(oldReport, oldFederalStateSectorReportValuesHashMap, oldRessortSectorReportValueHashMap);
        }

        //add all change types to the federalStateSectorReportValue
        federalStateSectorReportValueHashMap.forEach((sector, sectorReportValuesHashMap) -> {
            sectorReportValuesHashMap.forEach((federalState, sectorReportValue) -> {
                ReportValue oldSectorReportValue = null;
                if(oldReport != null) {
                    oldSectorReportValue = oldFederalStateSectorReportValuesHashMap.get(sector).get(federalState);
                }
                setValueChangedTypeByOldValue(sectorReportValue, oldSectorReportValue);
            });
        });

        //add all change types to the ressortSectorReportValue
        ressortSectorReportValueHashMap.forEach((sector, sectorReportValue) -> {
            ReportValue oldSectorReportValue = oldRessortSectorReportValueHashMap.get(sector);
            setValueChangedTypeByOldValue(sectorReportValue, oldSectorReportValue);
        });

        return new SectorReportValueAccessor(federalStateSectorReportValueHashMap, ressortSectorReportValueHashMap);
    }

    private void setValueChangedTypeByOldValue(ReportValue sectorReportValue, ReportValue oldSectorReportValue) {
        //The value change type is unequal, if there is no oldReport (the oldSectorReportValue is null)
        // and the value of the sectorReportValues is greater than 0
        if(oldSectorReportValue == null){
            if(sectorReportValue.getValue() > 0){
                sectorReportValue.setValueChangedType(ValueChangedType.UNEQUAL);
            }
        }else if(oldSectorReportValue.getValue() == 0 && sectorReportValue.getValue() > 0 ||
                oldSectorReportValue.getValue() > 0 && sectorReportValue.getValue() == 0){
            sectorReportValue.setValueChangedType(ValueChangedType.UNEQUAL);
        }else if(sectorReportValue.getValue() > oldSectorReportValue.getValue()){
            sectorReportValue.setValueChangedType(ValueChangedType.UP);
        }else if(sectorReportValue.getValue() < oldSectorReportValue.getValue()){
            sectorReportValue.setValueChangedType(ValueChangedType.DOWN);
        }
    }

    private void fillSectorReportValueHashMapsFromReport(Report report, HashMap<Sector,
            HashMap<FederalState, ReportValue>> federalStateSectorReportValuesHashMap,
             HashMap<Sector, ReportValue> ressortSectorReportValueHashMap) {

        for(Questionnaire questionnaire: report.getQuestionnaires()){
            for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
                ReportValue sectorReportValue;
                //handle stuff for the federalStateQuestionnaires
                if(questionnaire.getFederalState()!= null) {
                    HashMap<FederalState, ReportValue> federalStateReportValueHashMap = federalStateSectorReportValuesHashMap.get(branchQuestionnaire.getBranch().getSector());

                    //add the hashMap to the hashMap if not exists
                    if (federalStateReportValueHashMap == null) {
                        federalStateReportValueHashMap = new HashMap<>();
                        federalStateSectorReportValuesHashMap.put(branchQuestionnaire.getBranch().getSector(), federalStateReportValueHashMap);
                    }

                    sectorReportValue = federalStateReportValueHashMap.get(questionnaire.getFederalState());
                    //add sectorReportValues to the hashMap if not exists
                    if (sectorReportValue == null) {
                        sectorReportValue = new ReportValue();
                        federalStateReportValueHashMap.put(questionnaire.getFederalState(), sectorReportValue);
                    }

                }else{//handle stuff for the ressortQuestionnaires
                    sectorReportValue = ressortSectorReportValueHashMap.get(branchQuestionnaire.getBranch().getSector());
                    //add sectorReportValues to the hashMap if not exists
                    if (sectorReportValue == null) {
                        sectorReportValue = new ReportValue();
                        ressortSectorReportValueHashMap.put(branchQuestionnaire.getBranch().getSector(), sectorReportValue);
                    }
                }

                //check for each filledScenario if the value is greater than the value of zhe sectorReportValues
                //override if this is true
                for (FilledScenario filledScenario : branchQuestionnaire.getFilledScenarios()) {
                    if (filledScenario.getScenario().getScenarioType() == ScenarioType.AUSWAHL) {
                        if (sectorReportValue.getValue() < filledScenario.getValue()) {
                            sectorReportValue.setValue(filledScenario.getValue());
                        }
                    }
                }

            }
        }
    }
}
