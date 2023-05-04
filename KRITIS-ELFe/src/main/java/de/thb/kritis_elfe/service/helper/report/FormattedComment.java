package de.thb.kritis_elfe.service.helper.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormattedComment {
    String comment;
    List<String> commentParts;
    boolean yellow;

    public FormattedComment(String comment){
        this.comment = comment;
    }

    public void formatCommentFromOldComment(String oldComment){
        commentParts = new ArrayList<>();
        yellow = false;
        if(oldComment.length() == 0){
            yellow = true;
            commentParts.add(comment);
        }else {
            for (int i = 0; i < oldComment.length() && i < comment.length(); i++) {
                if (comment.charAt(i) != oldComment.charAt(i)) {
                    if (i == 0) {
                        yellow = true;
                        commentParts.add(comment);
                    } else {
                        commentParts.add(comment.substring(0, i - 1));
                        commentParts.add(comment.substring(i));
                    }
                    return;
                }
            }

            if(oldComment.length() == comment.length()){
                commentParts.add(comment);
            }else if(oldComment.length() < comment.length()){
                commentParts.add(comment.substring(0, oldComment.length() - 1));
                commentParts.add(comment.substring(oldComment.length()));
            }
        }


    }

    public List<String> getCommentParts() {
        return commentParts;
    }

    public boolean isYellow(){
        yellow = !yellow;
        return !yellow;
    }

    public String getComment(){
        return comment;
    }
}
