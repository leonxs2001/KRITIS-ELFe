package de.thb.kritis_elfe.controller.form;

import de.thb.kritis_elfe.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserFormModel {

    private List<User> users;
    private List<String> branchesAsString;
}
