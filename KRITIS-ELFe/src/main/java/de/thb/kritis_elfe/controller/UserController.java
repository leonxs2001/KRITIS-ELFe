package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.controller.form.ChangeCredentialsForm;
import de.thb.kritis_elfe.controller.form.ResetPasswordForm;
import de.thb.kritis_elfe.controller.form.ResetPasswordUserDataForm;
import de.thb.kritis_elfe.controller.form.UserRegisterFormModel;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.mail.EmailSender;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import javax.validation.Valid;

@Controller
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final FederalStateService federalStateService;
    private final RessortService ressortService;
    private final RoleService roleService;
    private final PasswordResetTokenService passwordResetTokenService;

    @GetMapping("/registrierung")
    public String showRegisterForm(Model model) {
        UserRegisterFormModel formModel = new UserRegisterFormModel();
        model.addAttribute("form", formModel);
        model.addAttribute("federalStates", federalStateService.getAllFederalStates());
        model.addAttribute("ressorts", ressortService.getAllRessorts());
        model.addAttribute("roles", roleService.getAllRoles());
        return "register/user_registration";
    }

    @PostMapping("/registrierung")
    public String registerUser(
            @ModelAttribute("user") @Valid UserRegisterFormModel formModel, BindingResult result) {
        if(result.hasErrors()){
            return "redirect:/registrierung?passwortUngleich";
        }
        try {
            userService.registerNewUser(formModel);
        } catch (UserAlreadyExistsException uaeEx) {
            return "redirect:/registrierung?usernameException";
        }

        return "register/success_register";
    }


    @GetMapping("/konto")
    public String showUserData(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "account/user_details";
    }

    @GetMapping(path = "/bestätigung/nutzer")
    public String userConfirmation(@RequestParam("token") String token) {
        userService.confirmUser(token);
        return "confirmation/confirmed_by_user";
    }

    @GetMapping(path = "/konto/ändere-daten")
    public String showChangePassword(){
        return "account/change_credentials";
    }

    @PostMapping(path = "konto/ändere-daten")
    public String changeCredentials(@Valid ChangeCredentialsForm form, Authentication authentication){

        try {
            User user = userService.getUserByUsername(authentication.getName());
            userService.changeCredentials(form,user);
        } catch (PasswordNotMatchingException e) {
            return "redirect:/konto/%C3%A4ndere-daten?passwordError";
        } catch (EmailNotMatchingException e) {
            return "redirect:/konto/%C3%A4ndere-daten?emailError";
        }

        return "redirect:/konto/%C3%A4ndere-daten?success";
    }

    @GetMapping("/passwort-reset")
    public String showPasswordReset() {
        return "security/password_reset";
    }

    @PostMapping("/passwort-reset")
    public String requestPasswordReset(@Valid ResetPasswordUserDataForm form) {
        passwordResetTokenService.createPasswordResetToken(form);
        return "redirect:/passwort-reset?success";

    }

    @GetMapping(path = "/reset-passwort")
    public String showResetPassword(@RequestParam("token") String token, Model model) throws TokenDoesNotExistException {

        if (passwordResetTokenService.getByToken(token) != null) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setToken(token);
            model.addAttribute("form", form);
            return "security/reset_password";
        } else {
            throw new TokenDoesNotExistException();
        }
    }

    @PostMapping(path = "/reset-passwort")
    public String resetUserPassword(@Valid ResetPasswordForm form, Model model) throws PasswordResetTokenExpired {
        try {
            model.addAttribute("form", form);
            passwordResetTokenService.resetUserPassword(form.getToken(), form);
        } catch (PasswordResetTokenExpired e) {
            return "redirect:/reset-passwort?tokenExpiredError&token="+form.getToken();
        } catch (PasswordNotMatchingException e){
            return "redirect:/reset-passwort?notMatchingError&token="+form.getToken();
        } catch (TokenAlreadyConfirmedException e){
            return "redirect:/reset-passwort?tokenConfirmedError&token="+form.getToken();
        }

        return "redirect:/reset-passwort?success";
    }

}





