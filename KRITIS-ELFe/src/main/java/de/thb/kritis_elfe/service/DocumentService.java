package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.service.helper.report.CommentReportValue;
import de.thb.kritis_elfe.service.helper.report.ReportValue;
import de.thb.kritis_elfe.service.helper.report.SectorBranchReportValueAccessor;
import de.thb.kritis_elfe.service.helper.report.SectorReportValueAccessor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;

@Service
public class DocumentService {


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

        XWPFTableRow federalStateTableRow = reportTable.getRow(0);

        //add Ressort head
        XWPFTableCell ressortTableCell = federalStateTableRow.getCell(1);

        ressortTableCell.setText("Bund");
        ressortTableCell.getParagraphs().get(0).setSpacingBefore(1);
        ressortTableCell.getParagraphs().get(0).setSpacingAfter(1);
        //Center shortcuts
        centerCellText(ressortTableCell);

        //create all federalStates
        for(int i = 0; i < federalStates.size(); i++){
            XWPFTableCell federalStateTableCell = federalStateTableRow.getCell(2 + i);
            //Center shortcuts
            centerCellText(federalStateTableCell);

            federalStateTableCell.setText(federalStates.get(i).getShortcut() );
        }

        for(int i = 0; i < sectors.size(); i++){
            XWPFTableRow xwpfTableRow = reportTable.getRow(1 + i);
            //add sector cell
            XWPFTableCell sectorTableCell = xwpfTableRow.getCell(0);
            sectorTableCell.setText((i +1) + ". " + sectors.get(i));
            //add Ressort value
            ReportValue sectorReportValue = sectorReportValueAccessor.getSectorRessortReportValue(sectors.get(i));
            setColorAndTextForCellFromSectorReportValue(xwpfTableRow.getCell(1), sectorReportValue);

            //create all federalStates
            for(int j = 0; j < federalStates.size(); j++){
                XWPFTableCell tableCell = xwpfTableRow.getCell(2 + j);
                sectorReportValue = sectorReportValueAccessor.getSectorFederalStateSectorReportValue(sectors.get(i), federalStates.get(j));
                setColorAndTextForCellFromSectorReportValue(tableCell, sectorReportValue);
            }
        }

        document.createParagraph().createRun().addBreak();

        document.write(outputStream);
        document.close();

    }

    //TODO delete duplicates!!!!!!! Sind viele
    public void createSectorReportWordDocument(OutputStream outputStream, Sector sector, List<FederalState> federalStates,
                                               SectorBranchReportValueAccessor sectorBranchReportValueAccessor) throws IOException {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun titleRun = title.createRun();
        titleRun.setText("Sektor " + sector.getName());
        titleRun.addBreak();
        titleRun.setBold(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(20);

        for(Branch branch: sector.getBranches()){
            XWPFParagraph branchParagraph = document.createParagraph();
            title.setAlignment(ParagraphAlignment.LEFT);

            XWPFRun branchTitleRun = branchParagraph.createRun();
            branchTitleRun.setText("Branche " + branch.getName());
            branchTitleRun.setFontSize(16);
            branchTitleRun.setBold(true);
            branchTitleRun.setUnderline(UnderlinePatterns.SINGLE);
            branchTitleRun.addBreak();

            XWPFTable branchTable = document.createTable(2, federalStates.size() + 1);

            XWPFParagraph commentParagraph = document.createParagraph();

            XWPFTableRow headTableRow = branchTable.getRow(0);

            XWPFTableCell headRessortTableCell = headTableRow.getCell(0);

            centerCellText(headRessortTableCell);

            String ressortHead = null;
            for(Ressort ressort: sectorBranchReportValueAccessor.getRessorts()){
                List<String> comments = sectorBranchReportValueAccessor.getRessortCommentReportValue(branch).getComments(ressort);
                createCommentRepresentationForComments(commentParagraph, ressort.getShortcut(), comments);

                if(ressortHead == null){
                    ressortHead = ressort.getName();
                }else{
                    ressortHead += " / " + ressort.getName();
                }
            }
            headRessortTableCell.setText(" " + ressortHead + " ");

            for(int i = 0; i < federalStates.size(); i++){
                XWPFTableCell headFederalStateTableCell = headTableRow.getCell(1 + i);
                centerCellText(headFederalStateTableCell);
                headFederalStateTableCell.setText(" " + federalStates.get(i).getShortcut() + " ");
            }

            XWPFTableRow branchTableRow = branchTable.getRow(1);

            XWPFTableCell branchRessortTableCell = branchTableRow.getCell(0);
            setColorAndTextForCellFromSectorReportValue(branchRessortTableCell, sectorBranchReportValueAccessor.getRessortCommentReportValue(branch));

            for(int i = 0; i < federalStates.size(); i++){
                FederalState federalState = federalStates.get(i);
                CommentReportValue commentReportValue = sectorBranchReportValueAccessor.getFederalStateBranchCommentReportValue(branch, federalState);
                XWPFTableCell branchFederalStateTableCell = branchTableRow.getCell(1 + i);
                setColorAndTextForCellFromSectorReportValue(branchFederalStateTableCell, commentReportValue);

                List<String> comments = commentReportValue.getComments();

                createCommentRepresentationForComments(commentParagraph, federalState.getShortcut(), comments);
            }


        }

        document.write(outputStream);
        document.close();
    }

    private void centerCellText(XWPFTableCell tableCell){
        tableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        tableCell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
        XWPFParagraph paragraph = tableCell.getParagraphs().get(0);
        paragraph.setSpacingBefore(1);
        paragraph.setSpacingAfter(1);
    }

    private void createCommentRepresentationForComments(XWPFParagraph commentParagraph, String shortcut, List<String> comments) {
        if(comments != null && comments.size() > 0) {
            XWPFRun commentRessortHeadRun = commentParagraph.createRun();
            commentRessortHeadRun.setBold(true);
            commentRessortHeadRun.setText(shortcut + ":");
            commentRessortHeadRun.addBreak();

            XWPFRun commentRun = commentParagraph.createRun();


            for (String comment : comments) {
                String[] singleCommentParts = comment.split("\n");
                commentRun.setText("- ");
                for(String singleCommentPart: singleCommentParts){
                    commentRun.setText(singleCommentPart);
                    commentRun.addBreak();
                }
            }
        }
    }

    private void setColorAndTextForCellFromSectorReportValue(XWPFTableCell xwpfTableCell, ReportValue sectorReportValue){
        //set color
        xwpfTableCell.setColor(sectorReportValue.getValueColorAsWordString());
        //center cell
        XWPFParagraph cellParagraph = xwpfTableCell.getParagraphs().get(0);
        centerCellText(xwpfTableCell);
        //set bold and size
        XWPFRun cellRun = cellParagraph.createRun();
        cellRun.setBold(true);
        cellRun.setFontSize(12);

        switch (sectorReportValue.getValueChangedType()){
            case UNEQUAL:
                cellRun.setText(" ≠ ");
                break;
            case UP:
                cellRun.setText(" ↑ ");
                break;
            case DOWN:
                cellRun.setText(" ↓ ");
                break;
        }
    }
}
