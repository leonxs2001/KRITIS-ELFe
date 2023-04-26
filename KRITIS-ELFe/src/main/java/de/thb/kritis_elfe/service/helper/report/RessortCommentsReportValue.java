package de.thb.kritis_elfe.service.helper.report;

import de.thb.kritis_elfe.entity.Ressort;

import java.util.HashMap;
import java.util.List;

public class RessortCommentsReportValue extends ReportValue{
    private HashMap<Ressort, List<String>> ressortComments;

    public RessortCommentsReportValue(){
        this(new HashMap<>());
    }

    public RessortCommentsReportValue(HashMap<Ressort, List<String>> ressortComments){
        super();
        this.ressortComments  = ressortComments;
    }

    public List<String> getComments(Ressort ressort){
        return ressortComments.get(ressort);
    }

    public HashMap<Ressort, List<String>> getRessortComments() {
        return ressortComments;
    }

    public void setRessortComments(HashMap<Ressort, List<String>> ressortComments) {
        this.ressortComments = ressortComments;
    }
}
