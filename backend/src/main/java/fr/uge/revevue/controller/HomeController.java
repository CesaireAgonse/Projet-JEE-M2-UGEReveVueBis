package fr.uge.revevue.controller;

import fr.uge.revevue.information.user.AuthInformation;
import fr.uge.revevue.service.CodeService;
import fr.uge.revevue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final UserService userService;
    private final CodeService codeService;

    @Autowired
    public HomeController(UserService userService, CodeService codeService){
        this.userService = userService;
        this.codeService = codeService;
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/")
    public String homePage(@RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
                           @RequestParam(value = "q", required = false, defaultValue = "") String query,
                           @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                           Model model) {
        var user = userService.currentUser();
        if (user != null){
            model.addAttribute("auth", AuthInformation.from(user));
        }
        model.addAttribute("filter", codeService.filter(sortBy, query, pageNumber, userService.currentUser()));
        return "home";
    }
}
