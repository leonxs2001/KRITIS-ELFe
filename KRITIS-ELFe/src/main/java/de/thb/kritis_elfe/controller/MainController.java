package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.configuration.KritisElfeReader;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.security.authority.UserAuthority;
import de.thb.kritis_elfe.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@AllArgsConstructor
public class MainController {
    private final UserService userService;
    private final KritisElfeReader kritisElfeReader;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("account")
    public String securedAccountPage() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().toString();

        if (role.contains(UserAuthority.USER)) {
            return "account/account_user";
        } else return "home";
    }


    @GetMapping("/setLogout")
    public void logintime(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        userService.setCurrentLogin(user);
    }

    @GetMapping(value="/help", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    byte[] getHelp() throws IOException {
        File file = new File(kritisElfeReader.getHelpPath() + "help.pdf");
        return IOUtils.toByteArray(new FileInputStream(file));
    }

    @GetMapping(path = "/datenschutz")
    public String showDataProtection(){
        return "datenschutz";
    }

}
