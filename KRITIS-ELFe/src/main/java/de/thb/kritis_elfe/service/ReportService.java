package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.Report;
import de.thb.kritis_elfe.repository.ReportRepository;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    QuestionnaireService questionnaireService;

    public List<Report> getAllSnapshots(){return reportRepository.findAll();}

    public List<Report> getAllSnapshotOrderByDESC(){return reportRepository.findAllByOrderByIdDesc();}

    public Report getReportById(Long id){return reportRepository.findById(id).get();}

    public Report getNewestSnapshot(){return reportRepository.findTopByOrderByIdDesc();}

    public boolean ExistsByName(String name){return reportRepository.existsSnapshotByName(name);}

    public void createReport(Report report){

        // Perist Snapshot
        report.setDate(LocalDateTime.now());
        reportRepository.save(report);

        questionnaireService.persistQuestionnairesForReport(report);

    }
}
