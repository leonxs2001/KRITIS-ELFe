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

    @GetMapping(value={"/", "/home"})
    public String home() {
        return "home";
    }

    @GetMapping(value="/hilfe", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody byte[] getHelp() throws IOException {
        String path = kritisElfeReader.getHelpPath();
        char lastChar = path.charAt(path.length() - 1);
        if(lastChar != '/'){
            path += "/";
        }
        File file = new File(path + "help.pdf");
        return IOUtils.toByteArray(new FileInputStream(file));
    }

    @GetMapping(path = "/datenschutz")
    public String showDataProtection(){
        return "datenschutz";
    }

}
