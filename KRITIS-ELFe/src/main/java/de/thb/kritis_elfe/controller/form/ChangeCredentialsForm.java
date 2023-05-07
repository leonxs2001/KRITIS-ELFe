package de.thb.kritis_elfe.controller.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeCredentialsForm {

    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
    private String oldEmail;
    private String newEmail;
    private String newUsername;
}
