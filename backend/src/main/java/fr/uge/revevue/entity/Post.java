package fr.uge.revevue.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Posts")
public abstract class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    protected Set<Vote> votes = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    protected Set<Comment> comments = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    public Set<Review> reviews = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    
    private long score;

    private Date date;

    @NotBlank
    @Column(columnDefinition = "VARCHAR(MAX)")
    private String title;

    @Column(name = "dtype", insertable = false, updatable = false)
    private String dtype;

    public Post(String title, User user) {
        this.title = title;
        this.user = user;
        this.date = new Date();
    }

    public Post() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public long getScore() {
        return score;
    }
    
    public void setScore(long score) {
        this.score = score;
    }

    public Vote.VoteType getVoteUser(String username){
        return votes.stream()
                .filter(vote -> vote.getUser().getUsername().equals(username))
                .map(Vote::getVoteType)
                .findFirst()
                .orElse(Vote.VoteType.NotVoted);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", votes=" + votes +
                ", comments=" + comments +
                ", reviews=" + reviews +
                ", user=" + user +
                ", score=" + score +
                ", date=" + date +
                ", title='" + title + '\'' +
                ", dtype='" + dtype + '\'' +
                '}';
    }
}
