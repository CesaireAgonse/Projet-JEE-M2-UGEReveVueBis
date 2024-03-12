package fr.uge.revevue.controller;

import fr.uge.revevue.form.PasswordForm;
import fr.uge.revevue.information.user.SimpleUserInformation;
import fr.uge.revevue.service.CodeService;
import fr.uge.revevue.service.CommentService;
import fr.uge.revevue.service.ReviewService;
import fr.uge.revevue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class UserController {
    private final UserService userService;
    private final CodeService codeService;
    private final ReviewService reviewService;
    private final CommentService commentService;

    @Autowired
    public UserController(UserService userService, CodeService codeService, ReviewService reviewService, CommentService commentService){
        this.userService = userService;
        this.codeService = codeService;
        this.reviewService = reviewService;
        this.commentService = commentService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/password")
    public String password(@ModelAttribute("passwordForm") PasswordForm passwordForm, Model model){
        model.addAttribute("auth", SimpleUserInformation.from(userService.currentUser()));
        return "users/password";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/password")
    public String password(@ModelAttribute("passwordForm") @Valid PasswordForm passwordForm, BindingResult result, Model model){
        var user = userService.currentUser();
        model.addAttribute("auth", SimpleUserInformation.from(user));
        if (result.hasErrors()){
            return "users/password";
        }
        if (!passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())){
            result.rejectValue("confirmPassword", "error.passwordForm", "The confirmation of the new password does not match the new password entered.");
            return "users/password";
        }
        if(userService.matchesPassword(passwordForm.getNewPassword(), user.getPassword())){
            result.rejectValue("newPassword", "error.passwordForm", "The new password should be different from the current password.");
            return "users/password";
        }
        if (!userService.matchesPassword(passwordForm.getCurrentPassword(), user.getPassword())) {
            result.rejectValue("currentPassword", "error.passwordForm", "The current password you entered is incorrect. Please try again.");
            return "users/password";
        }
        var userInformation = userService.modifyPassword(passwordForm.getCurrentPassword(), passwordForm.getNewPassword());
        return "redirect:/users/" + userInformation.username();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/users/{username}")
    public String information(@PathVariable String username,
                              @RequestParam(value = "codePageNumber", required = false)Integer codePageNumber,
                              @RequestParam(value = "reviewPageNumber", required = false)Integer reviewPageNumber,
                              @RequestParam(value = "commentPageNumber", required = false)Integer commentPageNumber,
                              Model model){
        var userInformation = userService.getInformation(username);
        if (userInformation == null){
            return "redirect:/";
        }
        var user = userService.currentUser();
        if (user != null){
            model.addAttribute("auth", userService.getInformation(user.getUsername()));
        }
        model.addAttribute("user", userInformation);

        if(codePageNumber == null || codePageNumber < 0) {
            codePageNumber = 0;
        }
        if(reviewPageNumber == null || reviewPageNumber < 0) {
            reviewPageNumber = 0;
        }
        if(commentPageNumber == null || commentPageNumber < 0) {
            commentPageNumber = 0;
        }
        model.addAttribute("codePageNumber", codePageNumber);
        model.addAttribute("reviewPageNumber", reviewPageNumber);
        model.addAttribute("commentPageNumber", commentPageNumber);

        var codesFromUser = codeService.getCodePageFromUserId(userInformation.id(), codePageNumber);
        var codesNumberFromUser = codeService.countCodesFromUser(userInformation);
        var reviewsFromUser = reviewService.getReviewPageFromUserId(userInformation.id(), reviewPageNumber);
        var reviewsNumberFromUser = reviewService.countReviewsFromUser(userInformation);
        var commentsFromUser = commentService.getCommentPageFromUserId(userInformation.id(), commentPageNumber);
        var commentsNumberFromUser = commentService.countCommentsFromUser(userInformation);
        model.addAttribute("codesFromUser", codesFromUser);
        model.addAttribute("codesNumberFromUser", codesNumberFromUser);
        model.addAttribute("reviewsFromUser", reviewsFromUser);
        model.addAttribute("reviewsNumberFromUser", reviewsNumberFromUser);
        model.addAttribute("commentsFromUser", commentsFromUser);
        model.addAttribute("commentsNumberFromUser", commentsNumberFromUser);
        return "users/profile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("users/follow/{username}")
    public String follow(@PathVariable String username){
        if (!userService.isExisted(username)){
            return "redirect:/";
        }
        userService.follow(userService.currentUser().getUsername(), username);
        return "redirect:/users/" + username;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("users/unfollow/{username}")
    public String unfollow(@PathVariable String username){
        if (!userService.isExisted(username)){
            return "redirect:/";
        }
        userService.unfollow(userService.currentUser().getUsername(), username);
        return "redirect:/users/" + username;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public String delete(@PathVariable("userId") @Valid long userId) {
        userService.delete(userId);
        return "redirect:/";
    }

}
