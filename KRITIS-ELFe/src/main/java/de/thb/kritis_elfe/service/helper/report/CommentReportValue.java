package de.thb.kritis_elfe.service.helper.report;

import de.thb.kritis_elfe.entity.Scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentReportValue extends ReportValue{
    HashMap<Scenario, FormattedComment> comments;

    public CommentReportValue(short value) {
        super(value);
        this.comments = new HashMap<>();
    }

    public CommentReportValue(){
        this((short)0);
    }

    public HashMap<Scenario, FormattedComment> getComments() {
        return comments;
    }

    public void setComments(HashMap<Scenario, FormattedComment> comments) {
        this.comments = comments;
    }
}
