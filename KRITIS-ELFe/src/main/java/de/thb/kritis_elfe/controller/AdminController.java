package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.configuration.KritisElfeReader;
import de.thb.kritis_elfe.controller.form.RessortsForm;
import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.EmptyFileException;
import de.thb.kritis_elfe.service.Exceptions.WrongContentTypeException;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
public class AdminController { //implements Comparable
    private final ReportService reportService;
    private final QuestionnaireService questionnaireService;
    private final RessortService ressortService;
    private final FederalStateService federalStateService;
    private final SectorService sectorService;
    private final DocumentService documentService;
    private final KritisElfeReader kritisElfeReader;

    @GetMapping("/report-control")
    public String getReportControl(Model model) {

        List<Report> reports = reportService.getAllReportsOrderByDESC();
        model.addAttribute("reports", reports);

        Report newReport = new Report();
        model.addAttribute("newReport", newReport);
        model.addAttribute("federalStatesWithNotUpdatedQuest", questionnaireService.getFederalStatesWithNotUpdatedQuestionnaire());
        model.addAttribute("ressortsWithNotUpdatedQuest", questionnaireService.getRessortsWithNotUpdatedQuestionnaire());
        model.addAttribute("ressorts", ressortService.getAllRessorts());
        model.addAttribute("federalStates", federalStateService.getAllFederalStates());
        return "report/report_control";
    }

    @PostMapping("/report-control")
    public String postNewReport(@ModelAttribute("snapName") Report newReport) {
        reportService.createReport(newReport);
        return "redirect:report-control";
    }

    @GetMapping("/report-details")
    public String showReportById(@RequestParam("id") long reportId, Model model) {
        Report report = reportService.getReportById(reportId);
        model.addAttribute("report", report);

        return "report/report_details";
    }

    @GetMapping("/adjustHelp")
    public String adjustHelp(){
        return "adjust_help";
    }

    @PostMapping("/adjustHelp")
    public String uploadNewHelpDocument(@RequestParam("file") MultipartFile file){
        try {
            documentService.savePDFFile(file, kritisElfeReader.getHelpPath() + "help.pdf");
        }catch (IOException e){
            return "redirect:adjustHelp?io_error";
        }catch (EmptyFileException e){
            return "redirect:adjustHelp?empty_error";
        }catch (WrongContentTypeException e){
            return "redirect:adjustHelp?wrong_type_error";
        }

        return "redirect:adjustHelp?success";
    }

    @GetMapping("/ressorts")
    public String getRessorts(Model model){
        List<Sector> sectors = sectorService.getAllSectors();
        RessortsForm ressortsForm =  new RessortsForm(ressortService.getAllRessorts());
        model.addAttribute("sectors", sectors);
        model.addAttribute("ressortForm",ressortsForm);
        return "ressorts";
    }

    @PostMapping("/ressorts")
    public String resetRessorts(@ModelAttribute RessortsForm ressortsForm){
        ressortService.resetRessortsByRessortsForm(ressortsForm);
        return "redirect:ressorts";
    }
}
