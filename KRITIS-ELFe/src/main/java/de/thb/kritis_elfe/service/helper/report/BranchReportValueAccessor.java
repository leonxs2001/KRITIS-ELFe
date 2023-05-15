package de.thb.kritis_elfe.service.helper.report;

import de.thb.kritis_elfe.entity.Branch;
import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Ressort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BranchReportValueAccessor {
    private HashMap<Branch, HashMap<FederalState, CommentReportValue>> federalStateBranchCommentReportValueHashMap;
    private HashMap<Branch, RessortCommentsReportValue> branchRessortCommentsReportValue;
    private Set<Ressort> ressorts;

    public BranchReportValueAccessor(HashMap<Branch, HashMap<FederalState, CommentReportValue>> federalStateBranchCommentReportValueHashMap, HashMap<Branch, RessortCommentsReportValue> branchRessortCommentsReportValue) {
        this.federalStateBranchCommentReportValueHashMap = federalStateBranchCommentReportValueHashMap;
        this.branchRessortCommentsReportValue = branchRessortCommentsReportValue;
        ressorts = new HashSet<>();
        //set Ressorts
        branchRessortCommentsReportValue.forEach((branch, ressortCommentsReportValue) -> {
            ressorts.addAll(ressortCommentsReportValue.getRessortComments().keySet());
        });
    }

    public CommentReportValue getFederalStateBranchCommentReportValue(Branch branch, FederalState federalState){
        return federalStateBranchCommentReportValueHashMap.get(branch).get(federalState);
    }

    public RessortCommentsReportValue getRessortCommentReportValue(Branch branch){
        return branchRessortCommentsReportValue.get(branch);
    }

    public Set<Ressort> getRessorts(){
        return ressorts;
    }


}
