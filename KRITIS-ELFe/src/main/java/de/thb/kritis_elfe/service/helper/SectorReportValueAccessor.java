package de.thb.kritis_elfe.service.helper;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Sector;

import java.util.HashMap;

public class SectorReportValueAccessor {
    private HashMap<Sector, HashMap<FederalState, SectorReportValue>> sectorFederalStateReportValues;
    private HashMap<Sector, SectorReportValue> sectorRessortReportValues;

    public SectorReportValueAccessor(HashMap<Sector, HashMap<FederalState, SectorReportValue>> sectorReportValues, HashMap<Sector, SectorReportValue> sectorRessortReportValues) {
        this.sectorFederalStateReportValues = sectorReportValues;
        this.sectorRessortReportValues = sectorRessortReportValues;
    }
    
    public SectorReportValue getSectorFederalStateSectorReportValue(Sector sector, FederalState federalState){
        SectorReportValue sectorReportValue = sectorFederalStateReportValues.get(sector).get(federalState);
        return sectorReportValue;
    }

    public SectorReportValue getSectorRessortReportValue(Sector sector){
        return sectorRessortReportValues.get(sector);
    }
}
