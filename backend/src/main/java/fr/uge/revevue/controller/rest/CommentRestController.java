package fr.uge.revevue.controller.rest;

import fr.uge.revevue.information.ReviewInformation;
import fr.uge.revevue.service.CommentService;
import fr.uge.revevue.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/comments")
public class CommentRestController {
    private final CommentService commentService;
    @Autowired
    public CommentRestController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> commentDeleted(@PathVariable("commentId") @Valid long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}