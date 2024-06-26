package fr.uge.revevue.controller;

import fr.uge.revevue.entity.Vote;
import fr.uge.revevue.form.CodeForm;
import fr.uge.revevue.form.CommentForm;
import fr.uge.revevue.form.ReviewForm;
import fr.uge.revevue.information.user.AuthInformation;
import fr.uge.revevue.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Objects;

@Controller
public class CodeController {
    private final UserService userService;
    private final CodeService codeService;
    private final VoteService voteService;
    private final CommentService commentService;
    private final ReviewService reviewService;

    @Autowired
    public CodeController(CodeService codeService, UserService userService, VoteService voteService, CommentService commentService, ReviewService reviewService){
        this.userService = userService;
        this.codeService = codeService;
        this.voteService = voteService;
        this.commentService = commentService;
        this.reviewService = reviewService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/codes/create")
    public String createPage(@ModelAttribute("codeForm") CodeForm codeForm, Model model){
        model.addAttribute("auth", AuthInformation.from(userService.currentUser()));
        return "codes/create";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/codes/{codeId}")
    public String get(@PathVariable("codeId") long codeId,
                       @ModelAttribute("commentForm") CommentForm commentForm,
                       @ModelAttribute("reviewForm") ReviewForm reviewForm,
                       @RequestParam(value = "reviewPageNumber", required = false) Integer reviewPageNumber,
                       @RequestParam(value = "sortBy", required = false, defaultValue = "newest") String sortBy,
                       @RequestParam(value = "commentPageNumber", required = false) Integer commentPageNumber,
                       @RequestParam(value = "oldContentPageNumber", required = false) Integer oldContentPageNumber,
                       Model model){
        var user = userService.currentUser();
        if (user != null){
            model.addAttribute("auth", AuthInformation.from(user));
            model.addAttribute("oldContentsReview", reviewService.reviewsContents(user.getUsername(), oldContentPageNumber));
        }
        var code = codeService.getInformation(codeId);
        if (code == null){
            throw new IllegalStateException("code not found");
        }
        model.addAttribute("code", code);
        model.addAttribute("reviewPageInformation", reviewService.getReviews(code.id(), sortBy, reviewPageNumber));
        model.addAttribute("commentPageInformation", commentService.getComments(code.id(), commentPageNumber));
        return "codes/codeReview";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/codes/create")
    public String create(@ModelAttribute @Valid CodeForm codeForm, BindingResult result)  throws IOException {
        if (result.hasErrors()){
            return "codes/create";
        }
        if (codeForm.getJavaFile().isEmpty()){
            result.rejectValue("javaFile", "error.codeForm", "Please upload a file.");
            return "codes/create";
        }
        if (!Objects.requireNonNull(codeForm.getJavaFile().getResource().getFilename()).endsWith(".java")){
            result.rejectValue("javaFile", "error.codeForm", "Please upload a java file.");
            return "codes/create";
        }
        if (!codeForm.getUnitFile().isEmpty() &&
                !Objects.requireNonNull(codeForm.getUnitFile().getResource().getFilename()).endsWith(".java")){
            result.rejectValue("unitFile", "error.codeForm", "Please upload a java file.");
            return "codes/create";
        }
        codeService.create(
                codeForm.getTitle(),
                codeForm.getDescription(),
                codeForm.getJavaFile(),
                codeForm.getUnitFile()
        );
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/codes/vote/{codeId}")
    public String codeVoted(@PathVariable("codeId") long codeId,
                            @RequestParam("voteType") Vote.VoteType voteType,
                            HttpServletRequest request){
        if (!codeService.isExisted(codeId)){
            return "redirect:/";
        }
        voteService.postVotedWithOptimisticLock(userService.currentUser().getId(), codeId, voteType);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/codes/" + codeId);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/codes/comment/{codeId}")
    public String codeCommented(@PathVariable("codeId") long codeId,
                                @ModelAttribute("commentForm") @Valid CommentForm commentForm,
                                BindingResult result){
        if (result.hasErrors()){
            return "redirect:/codes/" + codeId;
        }
        if (!codeService.isExisted(codeId)){
            return "redirect:/";
        }
        commentService.postCommented(codeId,commentForm.getContent(), commentForm.getCodeSelection());
        return "redirect:/codes/" + codeId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/codes/review/{codeId}")
    public String codeReviewed(@PathVariable("codeId") long codeId,
                               @ModelAttribute("reviewForm") @Valid ReviewForm reviewForm,
                               BindingResult result){
        if (result.hasErrors()){
            return "redirect:/codes/" + codeId;
        }
        if (!codeService.isExisted(codeId)){
            return "redirect:/";
        }
        reviewService.create(codeId, reviewForm.getTitle(), reviewForm.getContent());
        return "redirect:/codes/" + codeId;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/codes/delete/{codeId}")
    public String delete(@PathVariable("codeId") long codeId) {
        if (!codeService.isExisted(codeId)){
            return "redirect:/";
        }
        codeService.delete(codeId);
        return "redirect:/";
    }
}
