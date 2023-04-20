package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Report;
import de.thb.kritis_elfe.entity.Sector;
import de.thb.kritis_elfe.entity.questionnaire.BranchQuestionnaire;
import de.thb.kritis_elfe.entity.questionnaire.FilledScenario;
import de.thb.kritis_elfe.entity.questionnaire.Questionnaire;
import de.thb.kritis_elfe.enums.ScenarioType;
import de.thb.kritis_elfe.enums.ValueChangedType;
import de.thb.kritis_elfe.repository.ReportRepository;
import de.thb.kritis_elfe.service.helper.SectorReportValue;
import de.thb.kritis_elfe.service.helper.SectorReportValueAccessor;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
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


    public void createReportWordDocument(OutputStream outputStream, List<Sector> sectors, List<FederalState> federalStates,
                                         SectorReportValueAccessor sectorReportValueAccessor, Report report) throws IOException {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun titleRun = title.createRun();
        titleRun.setText("Lagebild Report");
        titleRun.addBreak();
        titleRun.addBreak();
        titleRun.setBold(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(20);

        XWPFRun reportRun = title.createRun();
        reportRun.setText( "Für den Report " + report);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(14);

        XWPFTable reportTable = document.createTable(sectors.size() + 1,federalStates.size() + 2);
        reportTable.setWidth(100);

        XWPFTableRow federalStateTableRow = reportTable.getRow(0);

        //add Ressort head
        XWPFTableCell ressortTableCell = federalStateTableRow.getCell(1);
        //ressortTableCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(4));
        ressortTableCell.setText(" Bund ");
        //Center shortcuts
        ressortTableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        ressortTableCell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);

        //create all federalStates
        for(int i = 0; i < federalStates.size(); i++){
            XWPFTableCell federalStateTableCell = federalStateTableRow.getCell(2 + i);
            //Center shortcuts
            federalStateTableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            federalStateTableCell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);

            federalStateTableCell.setText(" " + federalStates.get(i).getShortcut() + " ");
        }

        for(int i = 0; i < sectors.size(); i++){
            XWPFTableRow xwpfTableRow = reportTable.getRow(1 + i);
            //add sector cell
            XWPFTableCell sectorTableCell = xwpfTableRow.getCell(0);
            sectorTableCell.setText((i +1) + ". " + sectors.get(i));
            //add Ressort value
            SectorReportValue sectorReportValue = sectorReportValueAccessor.getSectorRessortReportValue(sectors.get(i));
            setColorAndTextForCellFromSectorReportValue(xwpfTableRow.getCell(1), sectorReportValue);

            //create all federalStates
            for(int j = 0; j < federalStates.size(); j++){
                XWPFTableCell tableCell = xwpfTableRow.getCell(2 + j);
                sectorReportValue = sectorReportValueAccessor.getSectorFederalStateSectorReportValue(sectors.get(i), federalStates.get(j));
                setColorAndTextForCellFromSectorReportValue(tableCell, sectorReportValue);
            }
        }

        document.write(outputStream);
        document.close();

    }

    private void setColorAndTextForCellFromSectorReportValue(XWPFTableCell xwpfTableCell, SectorReportValue sectorReportValue){
        //set color
        xwpfTableCell.setColor(sectorReportValue.getValueColorAsWordString());
        //center cell
        XWPFParagraph cellParagraph = xwpfTableCell.getParagraphs().get(0);
        xwpfTableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        cellParagraph.setAlignment(ParagraphAlignment.CENTER);
        //set bold and size
        XWPFRun cellRun = cellParagraph.createRun();
        cellRun.setBold(true);
        cellRun.setFontSize(12);

        switch (sectorReportValue.getValueChangedType()){
            case UNEQUAL:
                cellRun.setText(" ≠ ");
                break;
            case UP:
                cellRun.setText(" ↑ ");
                break;
            case DOWN:
                cellRun.setText(" ↓ ");
                break;
        }
    }

    public SectorReportValueAccessor createSectorReportValueAccessor(Report report){

        if(report == null){
            return null;
        }

        HashMap<Sector, HashMap<FederalState, SectorReportValue>> federalStateSectorReportValueHashMap = new HashMap<>();
        HashMap<Sector, SectorReportValue> ressortSectorReportValueHashMap = new HashMap<>();

        fillSectorReportValueHashMapsFromReport(report, federalStateSectorReportValueHashMap, ressortSectorReportValueHashMap);

        HashMap<Sector, HashMap<FederalState, SectorReportValue>> oldFederalStateSectorReportValuesHashMap = new HashMap<>();
        HashMap<Sector, SectorReportValue> oldRessortSectorReportValueHashMap = new HashMap<>();
        Report oldReport = reportRepository.findTopByIdLessThanOrderByIdDesc(report.getId());

        if(oldReport != null) {
            fillSectorReportValueHashMapsFromReport(oldReport, oldFederalStateSectorReportValuesHashMap, oldRessortSectorReportValueHashMap);
        }

        //add all change types to the federalStateSectorReportValue
        federalStateSectorReportValueHashMap.forEach((sector, sectorReportValuesHashMap) -> {
            sectorReportValuesHashMap.forEach((federalState, sectorReportValue) -> {
                SectorReportValue oldSectorReportValue = null;
                if(oldReport != null) {
                    oldSectorReportValue = oldFederalStateSectorReportValuesHashMap.get(sector).get(federalState);
                }
                setValueChangedTypeByOldValue(sectorReportValue, oldSectorReportValue);
            });
        });

        //add all change types to the ressortSectorReportValue
        ressortSectorReportValueHashMap.forEach((sector, sectorReportValue) -> {
            SectorReportValue oldSectorReportValue = oldRessortSectorReportValueHashMap.get(sector);
            setValueChangedTypeByOldValue(sectorReportValue, oldSectorReportValue);
        });

        return new SectorReportValueAccessor(federalStateSectorReportValueHashMap, ressortSectorReportValueHashMap);
    }

    private void setValueChangedTypeByOldValue(SectorReportValue sectorReportValue, SectorReportValue oldSectorReportValue) {
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
            HashMap<FederalState, SectorReportValue>> federalStateSectorReportValuesHashMap,
             HashMap<Sector, SectorReportValue> ressortSectorReportValueHashMap) {

        for(Questionnaire questionnaire: report.getQuestionnaires()){
            for(BranchQuestionnaire branchQuestionnaire: questionnaire.getBranchQuestionnaires()){
                SectorReportValue sectorReportValue;
                //handle stuff for the federalStateQuestionnaires
                if(questionnaire.getFederalState()!= null) {
                    HashMap<FederalState, SectorReportValue> federalStateReportValueHashMap = federalStateSectorReportValuesHashMap.get(branchQuestionnaire.getBranch().getSector());

                    //add the hashMap to the hashMap if not exists
                    if (federalStateReportValueHashMap == null) {
                        federalStateReportValueHashMap = new HashMap<>();
                        federalStateSectorReportValuesHashMap.put(branchQuestionnaire.getBranch().getSector(), federalStateReportValueHashMap);
                    }

                    sectorReportValue = federalStateReportValueHashMap.get(questionnaire.getFederalState());
                    //add sectorReportValues to the hashMap if not exists
                    if (sectorReportValue == null) {
                        sectorReportValue = new SectorReportValue();
                        federalStateReportValueHashMap.put(questionnaire.getFederalState(), sectorReportValue);
                    }

                }else{//handle stuff for the ressortQuestionnaires
                    sectorReportValue = ressortSectorReportValueHashMap.get(branchQuestionnaire.getBranch().getSector());
                    //add sectorReportValues to the hashMap if not exists
                    if (sectorReportValue == null) {
                        sectorReportValue = new SectorReportValue();
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
