package de.thb.kritis_elfe.controller.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form for the reset of the user password.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordForm {

    private String newPassword;
    private String confirmPassword;
    private String token;
}
