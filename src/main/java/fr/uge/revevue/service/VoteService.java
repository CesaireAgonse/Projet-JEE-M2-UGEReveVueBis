package fr.uge.revevue.service;


import fr.uge.revevue.entity.Code;
import fr.uge.revevue.entity.User;
import fr.uge.revevue.entity.Vote;
import fr.uge.revevue.repository.CodeRepository;
import fr.uge.revevue.repository.PostRepository;
import fr.uge.revevue.repository.UserRepository;
import fr.uge.revevue.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

@Service
public class VoteService {
    private VoteRepository voteRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;

    @PersistenceUnit
    private final EntityManagerFactory emf;

    @PersistenceContext
    private final EntityManager em;

    @Autowired
    public VoteService(VoteRepository voteRepository,
                       PostRepository postRepository,
                       UserRepository userRepository,
                       EntityManagerFactory emf,
                       EntityManager em){
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.emf = emf;
        this.em = em;
    }

    @Transactional
    public int codeVoted(long userId, long codeId, Vote.VoteType voteType){
        var findUser = userRepository.findById(userId);
        if (findUser.isEmpty()){
            throw new IllegalStateException("User not found");
        }
        var findCode = postRepository.findById(codeId);
        if (findCode.isEmpty()){
            throw new IllegalStateException("Code not found");
        }
        var user = findUser.get();
        var post = findCode.get();
        var vote = voteRepository.findByPostAndUser(user, post);
        if (vote == null){
            var newVote = new Vote(user, post, voteType);
            post.getVotes().add(newVote);
            voteRepository.save(newVote);
            return post.getScoreVote();
        }
        else if (vote.getVoteType() != voteType){
            vote.setVoteType(voteType);
            em.persist(vote);
            return post.getScoreVote();
        }
        post.getVotes().remove(vote);
        voteRepository.delete(vote);
        return post.getScoreVote();
    }
}
