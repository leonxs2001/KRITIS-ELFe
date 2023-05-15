package de.thb.kritis_elfe.service.helper.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedComment {
    private String comment;
    private List<String> commentParts;
    private boolean yellow;

    public FormattedComment(String comment){
        this.comment = comment;
    }

    public void formatCommentFromOldComment(String oldComment) {
        commentParts = new ArrayList<>();

        if (comment.length() != 0) {
            if (oldComment.length() == 0) {
                // the whole comment is new if there is no old comment
                commentParts.add(comment);
                yellow = true;
            } else{

                // go through all chars and check for changes
                int i = 0, j = 0;
                int lastIndex = 0;
                do{
                    if(comment.charAt(i) != oldComment.charAt(j)){
                        commentParts.add(comment.substring(lastIndex, i));
                        lastIndex = i;
                        int newI = getStartOfNewWord(i, comment);
                        int newJ = getStartOfNewWord(j, oldComment);

                        if(newI >= comment.length() || newJ >= oldComment.length()  || comment.charAt(newI) != oldComment.charAt(newJ)){
                            break;
                        }else{
                            commentParts.add(comment.substring(lastIndex, newI - 2));
                            i = newI + 2;
                            j = newJ + 2;
                            lastIndex = newI - 2;
                        }
                    }

                    i++;
                    j++;

                    if(i < comment.length() && j < oldComment.length() && Character.isWhitespace(comment.charAt(i)) && Character.isWhitespace(oldComment.charAt(j))){
                        while(i < comment.length() && Character.isWhitespace(comment.charAt(i))){
                            i++;
                        }

                        while(j < oldComment.length() && Character.isWhitespace(oldComment.charAt(j))){
                            j++;
                        }
                    }

                }while(i < comment.length() && j < oldComment.length());

                if(lastIndex != comment.length() && lastIndex != i){
                    commentParts.add(comment.substring(lastIndex, i));
                }

                if(i < comment.length()){
                    commentParts.add(comment.substring(i));
                }

                if(commentParts.size() > 0 && commentParts.get(0).equals("")){
                    yellow = true;
                    commentParts.remove(0);
                }
            }
        }
    }

    public void formatCommentWithoutOldComment(){
        formatCommentFromOldComment("");
    }

    private int getStartOfNewWord(int startIndex, String comment){
        String subComment = comment.substring(startIndex);
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(subComment);;

        if(matcher.find()){
            return startIndex + matcher.end() + 1;
        }

        return comment.length() - 1;
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
