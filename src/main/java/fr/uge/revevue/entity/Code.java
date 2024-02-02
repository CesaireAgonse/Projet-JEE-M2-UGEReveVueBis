package fr.uge.revevue.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Codes")
public class Code extends Post {

    @NotBlank
    private String title;

    @NotBlank
    @Column(columnDefinition = "VARCHAR(MAX)")
    private String description;

    @NotNull
    @Column(columnDefinition = "VARCHAR(MAX)")
    private String javaContent;

    @Column(columnDefinition = "VARCHAR(MAX)")
    private String unitContent;

    private Date date;



    public Code(){}

    public Code(User user, String title, String description, String javaContent) {
        super(user);
        this.title = title;
        this.description = description;
        this.javaContent = javaContent;
        this.date = new Date();
    }




    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public String getJavaContent() {return javaContent;}

    public void setJavaContent(String javaContent) {this.javaContent = javaContent;}

    public String getUnitContent() {return unitContent;}

    public void setUnitContent(String unitContent) {this.unitContent = unitContent;}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Vote> getVotes() {return votes;}

    public void setVotes(Set<Vote> votes) {this.votes = votes;}

    public int getScoreVote(){
        var score = 0;
        for (var vote : votes) {
            score += vote.getScore();
        }
        return score;
    }

    @Override
    public String toString() {
        return "Code{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", javaContent='" + javaContent + '\'' +
                ", unitContent='" + unitContent + '\'' +
                ", date=" + date +
                ", votes=" + votes +
                '}';
    }
}
