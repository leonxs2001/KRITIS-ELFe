package de.thb.kritis_elfe.service.helper.report;

import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.entity.Scenario;

import java.util.HashMap;
import java.util.List;

public class RessortCommentsReportValue extends ReportValue{
    private HashMap<Ressort, HashMap<Scenario, FormattedComment>> ressortComments;//Todo scenario rein

    public RessortCommentsReportValue(){
        this(new HashMap<>());
    }

    public RessortCommentsReportValue(HashMap<Ressort, HashMap<Scenario, FormattedComment>> ressortComments){
        super();
        this.ressortComments  = ressortComments;
    }

    public HashMap<Scenario, FormattedComment> getComments(Ressort ressort){
        return ressortComments.get(ressort);
    }

    public HashMap<Ressort, HashMap<Scenario, FormattedComment>> getRessortComments() {
        return ressortComments;
    }

    public void setRessortComments(HashMap<Ressort, HashMap<Scenario, FormattedComment>> ressortComments) {
        this.ressortComments = ressortComments;
    }


}
