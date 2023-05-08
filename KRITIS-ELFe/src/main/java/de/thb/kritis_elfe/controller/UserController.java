package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.controller.form.ChangeCredentialsForm;
import de.thb.kritis_elfe.controller.form.ResetPasswordForm;
import de.thb.kritis_elfe.controller.form.ResetPasswordUserDataForm;
import de.thb.kritis_elfe.controller.form.UserRegisterFormModel;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.service.*;
import de.thb.kritis_elfe.service.Exceptions.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    @Autowired
    SectorService sectorService;
    @Autowired
    FederalStateService federalStateService;
    @Autowired
    RessortService ressortService;
    @Autowired
    RoleService roleService;
    @Autowired
    PasswordResetTokenService passwordResetTokenService;

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
            model.addAttribute("form", formModel);
            model.addAttribute("federalStates", federalStateService.getAllFederalStates());
            model.addAttribute("ressorts", ressortService.getAllRessorts());
            model.addAttribute("roles", roleService.getAllRoles());
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

    @GetMapping("/password-reset")
    public String showPasswordReset() {
        return "security/password_reset";
    }

    @PostMapping("/password-reset")
    public String requestPasswordReset(@Valid ResetPasswordUserDataForm form, Model model) {

        try {
            model.addAttribute("form", form);
            User user = userService.getUserByUsername(form.getUsername());
            passwordResetTokenService.createPasswordResetToken(user);
            model.addAttribute("success", "Eingabe erfolgreich. Sofern Ihre Email einem Nutzer zugeordnet werden kann erhalten Sie demnächst eine Benachrichtigung per Mail.");

        } catch (EmailNotMatchingException e) {
            model.addAttribute("error", "Bei der Eingabe ist ein Fehler aufgetreten. Bitte stellen Sie sicher, dass die eingegebene Email auch korrekt ist.");
            return "security/password_reset";
        }
        return "security/password_reset";

    }

    @GetMapping(path = "/reset-password")
    public String showResetPassword(@RequestParam("token") String token, Model model) {

        if (passwordResetTokenService.getByToken(token) != null) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setToken(token);
            model.addAttribute("form", form);
            return "security/reset_password";
        } else return "Token existiert nicht";
    }

    @PostMapping(path = "/reset-password")
    public String resetUserPassword(@Valid ResetPasswordForm form, Model model) throws PasswordResetTokenExpired {

        try {
            model.addAttribute("form", form);
            passwordResetTokenService.resetUserPassword(form.getToken(), form);
                model.addAttribute("success", "Ihr Passwort wurde erfolgreich geändert.");
        } catch (PasswordResetTokenExpired e) {
            model.addAttribute("error", "Ihr Token zum Zurücksetzen des Passworts ist abgelaufen. Bitte beantragen Sie ihn erneut unter \" Passwort vergessen\".");
        } catch (PasswordNotMatchingException e){
            model.addAttribute("error", "Die beiden Passwörter müssen gleich sein.");
        } catch (TokenAlreadyConfirmedException e){
            model.addAttribute("error", "Das Passwort wurde bereits zurückgesetzt.");
        }

        return "security/reset_password";
    }

}





