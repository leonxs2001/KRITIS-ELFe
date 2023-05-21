package de.thb.kritis_elfe.controller.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form for the username and email of the user, whose password should be rested.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordUserDataForm {
    private String email;
    private String username;
}
