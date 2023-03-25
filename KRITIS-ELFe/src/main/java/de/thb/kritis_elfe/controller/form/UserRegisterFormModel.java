package de.thb.kritis_elfe.controller.form;

import de.thb.kritis_elfe.entity.FederalState;
import de.thb.kritis_elfe.entity.Ressort;
import de.thb.kritis_elfe.entity.Role;
import de.thb.kritis_elfe.security.passwordValidation.PasswordMatches;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches
public class UserRegisterFormModel {

    @NotNull(message = "LastName not null")
    @NotEmpty(message = "Nachname darf nicht leer sein")
    private String lastname;

    @NotNull(message = "firstName not null")
    @NotEmpty(message = "Vorname darf nicht leer sein")
    private String firstname;

    @NotNull(message = "password not null")
    @NotEmpty(message = "Passwort darf nicht leer sein")
    private String password;
    private String confirmPassword;

    @NotNull(message = "email not null")
    @NotEmpty(message = "Email darf nicht leer sein")
    @Email
    private String email;

    @NotNull(message = "username not null")
    @NotEmpty(message = "Username darf nicht leer sein")
    private String username;

    private Role role;

    private FederalState federalState;

    private Ressort ressort;


}
