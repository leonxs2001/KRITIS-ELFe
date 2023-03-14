package de.thb.webbaki.controller;

import de.thb.webbaki.controller.form.UserFormModel;
import de.thb.webbaki.entity.Sector;
import de.thb.webbaki.entity.User;
import de.thb.webbaki.repository.UserRepository;
import de.thb.webbaki.service.SectorService;
import de.thb.webbaki.service.UserService;
import de.thb.webbaki.service.BranchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;

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
        List<Sector> sectors = sectorService.getAllSectors();
        List<String> branchesAsString = new LinkedList<>();

        for(User user : users){
            //branchesAsString.add(user.getBranch().getName());
        }

        UserFormModel form = new UserFormModel();

        form.setUsers(users);
        form.setBranchesAsString(branchesAsString);

        model.addAttribute("form", form);
        model.addAttribute("sectorList", sectors);

        return "permissions/office";
    }

    @PostMapping("/office")
    public String deactivateUser(@ModelAttribute("form") @Valid UserFormModel form){
        System.out.println(form.getUsers());

        userService.changeEnabledStatus(form);
        //userService.changeBranch(form);

        return "redirect:office";
    }

}
