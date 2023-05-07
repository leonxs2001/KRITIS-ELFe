package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.configuration.HelpPathReader;
import de.thb.kritis_elfe.controller.form.ChangeCredentialsForm;
import de.thb.kritis_elfe.controller.form.UserRegisterFormModel;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.EmailNotMatchingException;
import de.thb.kritis_elfe.service.Exceptions.PasswordNotMatchingException;
import de.thb.kritis_elfe.service.Exceptions.UserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;

@Controller
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    @Autowired
    SectorService sectorService;
    @Autowired
    HelpPathReader helpPathReader;
    @Autowired
    FederalStateService federalStateService;
    @Autowired
    RessortService ressortService;
    @Autowired
    RoleService roleService;

    @GetMapping("/register/user")
    public String showRegisterForm(Model model) {
        UserRegisterFormModel formModel = new UserRegisterFormModel();
        model.addAttribute("form", formModel);
        model.addAttribute("federalStates", federalStateService.getAllFederalStates());
        model.addAttribute("ressorts", ressortService.getAllRessorts());
        model.addAttribute("roles", roleService.getAllRoles());
        return "register/user_registration";
    }

    @PostMapping("/register/user")
    public String registerUser(
            @ModelAttribute("user") @Valid UserRegisterFormModel formModel, BindingResult result,
            Model model) {

        try {
            userService.registerNewUser(formModel);

        } catch (UserAlreadyExistsException uaeEx) {
            model.addAttribute("usernameError", "Es existiert bereits ein Account mit diesem Nutzernamen.");
            return "register/user_registration";
        }

        return "register/success_register";
    }


    @GetMapping("/account/user_details")
    public String showUserData(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "account/user_details";
    }

    @GetMapping(path = "/confirmation/confirmByUser")
    public String userConfirmation(@RequestParam("token") String token) {
        userService.confirmUser(token);
        return "confirmation/confirmedByUser";
    }

    @GetMapping(path = "/confirmation/confirm")
    public String confirm(@RequestParam("token") String token) {
        userService.confirmTokenByAdmin(token);
        return "confirmation/confirm";
    }

    @GetMapping(path = "/account/changeCredentials")
    public String showChangePassword(){
        return "change_credentials";
    }

    @PostMapping(path = "account/changeCredentials")
    public String changeCredentials(@Valid ChangeCredentialsForm form, Principal principal, Model model){

        try {
            String username = principal.getName();
            User user = userService.getUserByUsername(username);

            model.addAttribute("user", user);
            model.addAttribute("form", form);

            userService.changeCredentials(form,user, model);
        } catch (PasswordNotMatchingException passEx) {
            model.addAttribute("passwordError", "Das eingegebene Password stimmt nicht mit Ihrem aktuellen Passwort überein.");
            return "change_credentials";
        } catch (EmailNotMatchingException e){
            model.addAttribute("emailError", "Die eingegebene Email-Adresse stimmt nicht mit Ihrer aktuellen Email überein.");
            return "change_credentials";
        }

        return "change_credentials";
    }

    @GetMapping(value="/help", produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody byte[] getHelp() throws IOException {
        File file = new File(helpPathReader.getPath() + "help.pdf");
        return IOUtils.toByteArray(new FileInputStream(file));
    }

}





