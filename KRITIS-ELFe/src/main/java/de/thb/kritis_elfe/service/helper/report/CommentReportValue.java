package de.thb.kritis_elfe.service.helper.report;

import java.util.ArrayList;
import java.util.List;

public class CommentReportValue extends ReportValue{
    List<String> comments;

    public CommentReportValue(short value) {
        super(value);
        this.comments = new ArrayList<>();
    }

    public CommentReportValue(){
        this((short)0);
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}
