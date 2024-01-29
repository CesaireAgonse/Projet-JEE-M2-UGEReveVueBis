package fr.uge.revevue.controller.rest;

import fr.uge.revevue.entity.Vote;
import fr.uge.revevue.service.CodeService;
import fr.uge.revevue.service.UserService;
import fr.uge.revevue.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/codes")
public class CodeRestController {
    private final UserService userService;
    private final CodeService codeService;
    private final VoteService voteService;

    @Autowired
    public CodeRestController(CodeService codeService, UserService userService, VoteService voteService){
        this.userService = userService;
        this.codeService = codeService;
        this.voteService = voteService;
    }

    @PostMapping("/vote/{codeId}")
    public ResponseEntity<Integer> codeVoted(@PathVariable("codeId") @Valid long codeId,
                                             @RequestParam("voteType") Vote.VoteType voteType) {
        return ResponseEntity.ok(voteService.codeVoted(userService.currentUser().getId(), codeId, voteType));
    }

}
