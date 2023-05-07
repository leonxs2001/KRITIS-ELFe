package de.thb.kritis_elfe.controller.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordUserDataForm {

    private String email;
    private String username;
}
