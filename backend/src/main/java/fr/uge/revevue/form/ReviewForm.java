package fr.uge.revevue.form;

import fr.uge.revevue.entity.Comment;
import fr.uge.revevue.entity.Review;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class ReviewForm {

    @NotBlank
    private String title;

    private List<CommentForm> content = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CommentForm> getContent() {
        return content;
    }

    public void setContents(List<CommentForm> content) {
        this.content = content;
    }
}
