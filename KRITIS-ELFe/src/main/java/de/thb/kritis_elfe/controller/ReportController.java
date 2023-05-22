package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.entity.Report;
import de.thb.kritis_elfe.entity.Sector;
import de.thb.kritis_elfe.service.DocumentService;
import de.thb.kritis_elfe.service.Exceptions.EntityDoesNotExistException;
import de.thb.kritis_elfe.service.FederalStateService;
import de.thb.kritis_elfe.service.SectorService;
import de.thb.kritis_elfe.service.helper.report.BranchReportValueAccessor;
import de.thb.kritis_elfe.service.helper.report.SectorReportValueAccessor;
import de.thb.kritis_elfe.service.ReportService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final SectorService sectorService;
    private final FederalStateService federalStateService;
    private final DocumentService documentService;


    @GetMapping("/report")
    public String showReport(@RequestParam(value = "id", required = false) Long reportId, Model model){
        Report report = reportService.getReportFromIdWithDefault(reportId);

        model.addAttribute("sectors", sectorService.getAllSectors());
        model.addAttribute("federalStates", federalStateService.getAllFederalStates());

        model.addAttribute("report", report);
        model.addAttribute("reports", reportService.getAllReportsOrderByDESC());

        SectorReportValueAccessor sectorReportValueAccessor = reportService.createSectorReportValueAccessor(report);
        model.addAttribute("sectorReportValueAccessor", sectorReportValueAccessor);

        return "report/report";
    }

    @GetMapping("report/download")
    public void downloadReport(@RequestParam(value = "id", required = false) Long reportId,
                               HttpServletResponse response) throws IOException {
        Report report = reportService.getReportFromIdWithDefault(reportId);

        documentService.setWordResponseHeader(response, report + ".docx");

        SectorReportValueAccessor sectorReportValueAccessor = reportService.createSectorReportValueAccessor(report);

        documentService.createReportWordDocument(response.getOutputStream(), sectorService.getAllSectors(),
                federalStateService.getAllFederalStates(), sectorReportValueAccessor, report);

    }

    @GetMapping("report/sector/download")
    public void downloadReport(@RequestParam(value = "reportId", required = false) Long reportId,
                               @RequestParam(value = "sectorId", required = false) Long sectorId,
                               HttpServletResponse response) throws IOException, EntityDoesNotExistException {
        Report report = reportService.getReportFromIdWithDefault(reportId);

        Optional<Sector> sector = sectorService.getSectorById(sectorId);
        if(!sector.isPresent()){
            throw new EntityDoesNotExistException("The Sector does not exists.");
        }

        documentService.setWordResponseHeader(response, "Sektorreport für den Sektor " + sector.get() + " vom Report " + report + ".docx");
        BranchReportValueAccessor branchReportValueAccessor = reportService.createSectorBranchReportValueAccessor(report, sector.get());

        documentService.createSectorReportWordDocument(response.getOutputStream(), sector.get(), federalStateService.getAllFederalStates(),
                branchReportValueAccessor);
    }

}