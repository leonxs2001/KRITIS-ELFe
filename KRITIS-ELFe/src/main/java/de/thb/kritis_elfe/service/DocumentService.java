package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.service.Exceptions.EmptyFileException;
import de.thb.kritis_elfe.service.Exceptions.WrongContentTypeException;
import de.thb.kritis_elfe.service.helper.report.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@Service
public class DocumentService {
    /**
     * Saves a given File on the given path.
     * @param file
     * @param pathString
     * @throws EmptyFileException
     * @throws WrongContentTypeException
     * @throws IOException
     */
    public void savePDFFile(MultipartFile file, String pathString) throws EmptyFileException, WrongContentTypeException, IOException {
        if(file.isEmpty()){
            throw new EmptyFileException("The file is empty.");
        }
        if(!file.getContentType().equals("application/pdf")){
            throw new WrongContentTypeException("The File is not a pdf file.");
        }
        byte[] bytes = file.getBytes();
        Path path = Paths.get(pathString);
        Files.write(path, bytes);
    }

    /**
     * Creates the classical report as word document and write it in the given output stream.
     * @param outputStream
     * @param sectors
     * @param federalStates
     * @param sectorReportValueAccessor
     * @param report
     * @throws IOException
     */
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

    //TODO delete duplicates!!!!!!! Sind viele und ordnen
    /**
     * Creates the sector report as word document and write it in the given output stream.
     * @param outputStream
     * @param sector
     * @param federalStates
     * @param branchReportValueAccessor
     * @throws IOException
     */
    public void createSectorReportWordDocument(OutputStream outputStream, Sector sector, List<FederalState> federalStates,
                                               BranchReportValueAccessor branchReportValueAccessor) throws IOException {
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

            String ressortHead = "";
            for(Ressort ressort: branchReportValueAccessor.getRessorts()){
                HashMap<Scenario, FormattedComment> comments = branchReportValueAccessor.getRessortCommentReportValue(branch).getComments(ressort);
                createCommentRepresentationForComments(commentParagraph, ressort.getShortcut(), comments);

                if(ressortHead.isEmpty()){
                    ressortHead = ressort.getName();
                }else{
                    ressortHead += " / " + ressort.getName();
                }
            }

            if (ressortHead.isEmpty()) {
                ressortHead = "/";
            }
            headRessortTableCell.setText(" " + ressortHead + " ");

            for(int i = 0; i < federalStates.size(); i++){
                XWPFTableCell headFederalStateTableCell = headTableRow.getCell(1 + i);
                centerCellText(headFederalStateTableCell);
                headFederalStateTableCell.setText(" " + federalStates.get(i).getShortcut() + " ");
            }

            XWPFTableRow branchTableRow = branchTable.getRow(1);

            XWPFTableCell branchRessortTableCell = branchTableRow.getCell(0);
            setColorAndTextForCellFromSectorReportValue(branchRessortTableCell, branchReportValueAccessor.getRessortCommentReportValue(branch));

            for(int i = 0; i < federalStates.size(); i++){
                FederalState federalState = federalStates.get(i);
                CommentReportValue commentReportValue = branchReportValueAccessor.getFederalStateBranchCommentReportValue(branch, federalState);
                XWPFTableCell branchFederalStateTableCell = branchTableRow.getCell(1 + i);
                setColorAndTextForCellFromSectorReportValue(branchFederalStateTableCell, commentReportValue);

                HashMap<Scenario, FormattedComment> comments = commentReportValue.getComments();

                createCommentRepresentationForComments(commentParagraph, federalState.getShortcut(), comments);
            }


        }

        document.write(outputStream);
        document.close();
    }

    /**
     * Centers the text in a given XWPFTableCell
     * @param tableCell
     */
    private void centerCellText(XWPFTableCell tableCell){
        tableCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        tableCell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
        XWPFParagraph paragraph = tableCell.getParagraphs().get(0);
        paragraph.setSpacingBefore(1);
        paragraph.setSpacingAfter(1);
    }

    /**
     * Adds the given comments to the given paragraph with assignment to the shortcut of the institution and all color information.
     * @param commentParagraph
     * @param shortcut
     * @param comments
     */
    private void createCommentRepresentationForComments(XWPFParagraph commentParagraph, String shortcut, HashMap<Scenario, FormattedComment> comments) {
        if(comments != null && comments.size() > 0) {
            XWPFRun commentRessortHeadRun = commentParagraph.createRun();
            commentRessortHeadRun.setBold(true);
            commentRessortHeadRun.setText(shortcut + ":");
            commentRessortHeadRun.addBreak();

            comments.forEach((scenario, formattedComment) -> {
                commentParagraph.createRun().setText("- ");
                for(String coloredComment: formattedComment.getCommentParts()){
                    XWPFRun commentRun = commentParagraph.createRun();
                    if(formattedComment.isYellow()){
                        commentRun.getCTR().addNewRPr().addNewHighlight().setVal(STHighlightColor.YELLOW);
                    }
                    writeTextInRunWithBreaks(commentRun, coloredComment);
                }
                commentParagraph.createRun().addBreak();
            });

            commentParagraph.createRun().addBreak();
        }
    }

    /**
     * Write the given Text in the given run and add all the "next lines".
     * Otherwise their where ignored.
     * @param run
     * @param text
     */
    private void writeTextInRunWithBreaks(XWPFRun run, String text){
        String[] singleTexts = text.split("\n");
        for(int i = 0; i < singleTexts.length; i++){
            run.setText(singleTexts[i]);
            if(i != singleTexts.length - 1) {
                run.addBreak();
            }
        }
    }

    /**
     * Set the Text and color of the given cell from the given reportvalue.
     * @param xwpfTableCell
     * @param sectorReportValue
     */
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

    /**
     * Sets the response Header for the given response to immediately download the word document with the given filename.
     * @param response
     * @param filename
     */
    public void setWordDownloadResponseHeader(HttpServletResponse response, String filename){
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + filename;
        response.setHeader(headerKey, headerValue);
    }

    /**
     * Extract the text from the given file dependent on its content type.
     * @param file
     * @return
     */
    public String getTextFromFile(MultipartFile file) {
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
