package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.configuration.HelpPathReader;
import de.thb.kritis_elfe.controller.form.RessortsForm;
import de.thb.kritis_elfe.controller.form.UserToRoleFormModel;
import de.thb.kritis_elfe.entity.*;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Controller
@AllArgsConstructor
public class SuperAdminController implements Comparable {
    private final UserService userService;
    private final RoleService roleService;
    private final ReportService reportService;
    private final QuestionnaireService questionnaireService;
    private final RessortService ressortService;
    private final FederalStateService federalStateService;
    private final SectorService sectorService;

    @Autowired
    HelpPathReader helpPathReader;

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

    @GetMapping("/confirmation/userDenied")
    public String userDenied() {
        return "confirmation/userDenied";
    }

    @GetMapping("/report-details")
    public String showSnapByID(@RequestParam("id") long reportId, Model model) {
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
        if(!file.isEmpty() && file.getContentType().equals("application/pdf")){
            try{
                byte[] bytes = file.getBytes();
                Path path = Paths.get(helpPathReader.getPath() + "help.pdf");
                Files.write(path, bytes);
                return "redirect:adjustHelp?success";
            } catch (IOException exception){
                return "redirect:adjustHelp?failure";
            }
        }else{
            return "redirect:adjustHelp?failure";
        }

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


    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
