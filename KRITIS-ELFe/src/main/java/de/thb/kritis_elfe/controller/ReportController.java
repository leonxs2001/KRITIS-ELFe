package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.entity.Report;
import de.thb.kritis_elfe.service.FederalStateService;
import de.thb.kritis_elfe.service.SectorService;
import de.thb.kritis_elfe.service.helper.SectorReportValueAccessor;
import de.thb.kritis_elfe.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//TODO - only report !!! nor reportfocus
@Controller
public class ReportController {
    @Autowired
    private ReportService reportService;

    @Autowired
    private SectorService sectorService;

    @Autowired
    private FederalStateService federalStateService;


    @GetMapping("/report")
    public String showReport(@RequestParam(value = "id", required = false) Long reportId, Model model){
        Report report;
        if(reportId == null){
            report = reportService.getNewestReport();
        }else{
            report = reportService.getReportById(reportId);
        }

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
        Report report;
        if(reportId == null){
            report = reportService.getNewestReport();
        }else{
            report = reportService.getReportById(reportId);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + report + ".docx";
        response.setHeader(headerKey, headerValue);
        Context context = new Context();

        context.setVariable("sectors", sectorService.getAllSectors());
        context.setVariable("federalStates", federalStateService.getAllFederalStates());

        SectorReportValueAccessor sectorReportValueAccessor = reportService.createSectorReportValueAccessor(report);

        reportService.createReportWordDocument(response.getOutputStream(), sectorService.getAllSectors(),
                federalStateService.getAllFederalStates(), sectorReportValueAccessor, report);

    }

}