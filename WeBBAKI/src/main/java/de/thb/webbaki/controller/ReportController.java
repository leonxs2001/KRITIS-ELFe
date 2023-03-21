package de.thb.webbaki.controller;

import com.lowagie.text.DocumentException;
import de.thb.webbaki.entity.Snapshot;
import de.thb.webbaki.service.Exceptions.UnknownReportFocusException;
import de.thb.webbaki.service.ReportService;
import de.thb.webbaki.service.helper.Counter;
import de.thb.webbaki.service.SnapshotService;
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
    private SnapshotService snapshotService;

    @Autowired
    private ReportService reportService;

    /**
     * Redirect to url with newest snap-id (because of the Nav elements in the layout.html).
     * @param reportFocusString
     */
    @GetMapping("report/{reportFocus}")
    public String showReport(@PathVariable("reportFocus") String reportFocusString){
        long snapId = snapshotService.getNewestSnapshot().getId();
        return "redirect:/report/"+reportFocusString+"/"+String.valueOf(snapId);
    }

    @GetMapping("report/{reportFocus}/{snapId}")
    public String showReport(@PathVariable("reportFocus") String reportFocusString, @PathVariable("snapId") long snapId,
                             Model model, Authentication authentication) throws UnknownReportFocusException {
        //ReportFocus reportFocus = ReportFocus.getReportFocusByEnglishRepresentation(reportFocusString);
        //model.addAttribute("reportFocus", reportFocus);

        Snapshot currentSnapshot = snapshotService.getSnapshotByID(snapId).get();
        model.addAttribute("currentSnapshot", currentSnapshot);

        final List<Snapshot> snapshotList = snapshotService.getAllSnapshotOrderByDESC();
        model.addAttribute("snapshotList", snapshotList);

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

        Snapshot currentSnapshot = snapshotService.getSnapshotByID(snapId).get();
        context.setVariable("currentSnapshot", currentSnapshot);

        context.setVariable("counter", new Counter());

        reportService.generatePdfFromHtml(reportService.parseThymeleafTemplateToHtml("report/report", context),
                request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort(), response.getOutputStream());

    }

}