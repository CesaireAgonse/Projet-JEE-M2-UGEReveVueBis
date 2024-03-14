package fr.uge.revevue.information.code;

import fr.uge.revevue.entity.Code;
import fr.uge.revevue.entity.Vote;
import fr.uge.revevue.information.comment.CommentInformation;
import fr.uge.revevue.information.review.ReviewInformation;
import fr.uge.revevue.information.user.SimpleUserInformation;
import fr.uge.revevue.information.UnitTestResultInformation;

import java.util.*;

public record CodeInformation(
        long id,
        SimpleUserInformation userInformation,
        Vote.VoteType voteType,
        String title,
        String description,
        String javaContent,
        String unitContent,
        UnitTestResultInformation testResultsInformation,
        long score,
        Date date,
        List<CommentInformation> comments,
        List<ReviewInformation> reviews
) {

    public static CodeInformation from(Code code){
        Objects.requireNonNull(code, "[CodeInformation] code is null");
        return new CodeInformation(
                code.getId(),
                SimpleUserInformation.from(code.getUser()),
                code.getVoteUser(),
                code.getTitle(),
                code.getDescription(),
                code.getJavaContent(),
                code.getUnitContent(),
                UnitTestResultInformation.from(code.getTestResults()),
                code.getScore(),
                code.getDate(),
                code.getComments().stream().map(CommentInformation::from).sorted(Comparator.comparing(CommentInformation::date).reversed()).toList(),
                code.getReviews().stream().map(ReviewInformation::from)
                        .sorted(Comparator.comparing(ReviewInformation::date).reversed())
                        .sorted(Comparator.comparing(ReviewInformation::score).reversed()).toList()
        );
    }
}
