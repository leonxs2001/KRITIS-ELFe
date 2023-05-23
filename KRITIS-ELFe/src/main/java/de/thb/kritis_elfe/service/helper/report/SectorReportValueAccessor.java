package de.thb.kritis_elfe.service.helper.report;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Sector;

import java.util.HashMap;

public class SectorReportValueAccessor {
    private HashMap<Sector, HashMap<FederalState, ReportValue>> sectorFederalStateReportValues;
    private HashMap<Sector, ReportValue> sectorRessortReportValues;

    public SectorReportValueAccessor(HashMap<Sector, HashMap<FederalState, ReportValue>> sectorReportValues, HashMap<Sector, ReportValue> sectorRessortReportValues) {
        this.sectorFederalStateReportValues = sectorReportValues;
        this.sectorRessortReportValues = sectorRessortReportValues;
    }
    
    public ReportValue getSectorFederalStateSectorReportValue(Sector sector, FederalState federalState){
        ReportValue sectorReportValue = sectorFederalStateReportValues.get(sector).get(federalState);
        return sectorReportValue;
    }

    public ReportValue getSectorRessortReportValue(Sector sector){
        ReportValue reportValue = sectorRessortReportValues.get(sector);
        return reportValue;
    }
}
