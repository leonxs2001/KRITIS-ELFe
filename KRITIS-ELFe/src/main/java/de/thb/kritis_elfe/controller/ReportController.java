package de.thb.kritis_elfe.controller;

import com.lowagie.text.DocumentException;
import de.thb.kritis_elfe.entity.Report;
import de.thb.kritis_elfe.service.Exceptions.UnknownReportFocusException;
import de.thb.kritis_elfe.service.ooooooldReportService;
import de.thb.kritis_elfe.service.helper.Counter;
import de.thb.kritis_elfe.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//TODO - only report !!! nor reportfocus
@Controller
public class ReportController {
    @Autowired
    private ReportService reportService;

    @Autowired
    private ooooooldReportService ooooooldReportService;

    /**
     * Redirect to url with newest snap-id (because of the Nav elements in the layout.html).
     * @param reportFocusString
     */
    @GetMapping("report/{reportFocus}")
    public String showReport(@PathVariable("reportFocus") String reportFocusString){
        long snapId = reportService.getNewestReport().getId();
        return "redirect:/report/"+reportFocusString+"/"+String.valueOf(snapId);
    }

    @GetMapping("report/{reportFocus}/{snapId}")
    public String showReport(@PathVariable("reportFocus") String reportFocusString, @PathVariable("snapId") long snapId,
                             Model model, Authentication authentication) throws UnknownReportFocusException {
        //ReportFocus reportFocus = ReportFocus.getReportFocusByEnglishRepresentation(reportFocusString);
        //model.addAttribute("reportFocus", reportFocus);

        Report currentReport = reportService.getReportById(snapId);
        model.addAttribute("currentSnapshot", currentReport);

        final List<Report> reportList = reportService.getAllReportsOrderByDESC();
        model.addAttribute("snapshotList", reportList);

        model.addAttribute("counter", new Counter());

        return "report/report_container";
    }

    @GetMapping("report/{reportFocus}/{snapId}/download")
    public void downloadReportPdf(@PathVariable("reportFocus") String reportFocusString, @PathVariable("snapId") long snapId,
                                  HttpServletResponse response, Authentication authentication, HttpServletRequest request) throws UnknownReportFocusException, IOException, DocumentException {

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=report.pdf";
        response.setHeader(headerKey, headerValue);
        Context context = new Context();

        /*ReportFocus reportFocus = ReportFocus.getReportFocusByEnglishRepresentation(reportFocusString);
        context.setVariable("reportFocus", reportFocus);*/

        Report currentReport = reportService.getReportById(snapId);
        context.setVariable("currentSnapshot", currentReport);

        context.setVariable("counter", new Counter());

        ooooooldReportService.generatePdfFromHtml(ooooooldReportService.parseThymeleafTemplateToHtml("report/report", context),
                request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort(), response.getOutputStream());

    }

}