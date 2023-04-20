package de.thb.kritis_elfe.controller;

import de.thb.kritis_elfe.controller.form.UserFormModel;
import de.thb.kritis_elfe.repository.UserRepository;
import de.thb.kritis_elfe.service.SectorService;
import de.thb.kritis_elfe.service.UserService;
import de.thb.kritis_elfe.service.BranchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@AllArgsConstructor
@SessionAttributes("form")
public class OfficeController {

    UserService userService;
    UserRepository userRepository;
    SectorService sectorService;
    BranchService branchService;

    @GetMapping("/office")
    public String showOfficePage(Model model){
        final var users = userService.getAllUsers();

        UserFormModel form = new UserFormModel();

        form.setUsers(users);

        model.addAttribute("form", form);

        return "permissions/office";
    }

    @PostMapping("/office")
    public String deactivateUser(@ModelAttribute("form") @Valid UserFormModel form){

        userService.changeEnabledStatusFromForm(form);

        return "redirect:office";
    }

}
