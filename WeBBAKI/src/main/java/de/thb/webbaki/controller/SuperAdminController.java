package de.thb.webbaki.controller;

import de.thb.webbaki.configuration.HelpPathReader;
import de.thb.webbaki.controller.form.UserToRoleFormModel;
import de.thb.webbaki.entity.Snapshot;
import de.thb.webbaki.entity.User;
import de.thb.webbaki.service.RoleService;
import de.thb.webbaki.service.ScenarioService;
import de.thb.webbaki.service.SnapshotService;
import de.thb.webbaki.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Controller
@AllArgsConstructor
public class SuperAdminController implements Comparable {
    private final UserService userService;
    private final RoleService roleService;
    private final SnapshotService snapshotService;

    @Autowired
    HelpPathReader helpPathReader;

    @GetMapping("/admin")
    public String showAllUsers(Model model) {
        final var users = userService.getAllUsers();
        List<String> emptyUsers = Arrays.asList(new String[users.size()]);
        UserToRoleFormModel formModel = UserToRoleFormModel.builder()
                .users(users)
                .role(emptyUsers)
                .roleDel(emptyUsers)
                .build();

        model.addAttribute("roleForm", formModel);
        final var roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);

        return "permissions/admin";
    }

    @PostMapping("/admin")
    public String addRoleToUser(@ModelAttribute("roleForm") @Valid UserToRoleFormModel userToRoleFormModel, Model model) {
        System.out.println(userToRoleFormModel.toString());

        userService.addAndDeleteRoles(userToRoleFormModel);

        List<User> users = userService.getAllUsers();

        model.addAttribute("roleForm", userToRoleFormModel);
        final var roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);

        return "redirect:admin";
    }

    @GetMapping("/snap")
    public String getSnap(Model model) {

        List<Snapshot> snaps = snapshotService.getAllSnapshots();
        model.addAttribute("snaps", snaps);

        Snapshot snapName = new Snapshot();
        model.addAttribute("snapName", snapName);

        return "snap/snapshot";
    }

    @GetMapping("/confirmation/userDenied")
    public String userDenied() {
        return "confirmation/userDenied";
    }

    @PostMapping("/snap")
    public String postSnap(@ModelAttribute("snapName") Snapshot snapName) {
        snapshotService.createSnap(snapName);
        return "redirect:snap";

    }

    @GetMapping("/snap/{snapID}")
    public String showSnapByID(@PathVariable("snapID") long snapID, Model model) {
        Snapshot snapshot = snapshotService.getSnapshotByID(snapID).get();
        model.addAttribute("snapshot", snapshot);

        return "snap/details";
    }

    @GetMapping("/adjustHelp")
    public String adjustHelp(){
        return "adjust_help";
    }

    @PostMapping("/adjustHelp")
    public String uploadNewHelpDocument(@RequestParam("file") MultipartFile file){
        if(!file.isEmpty() && file.getContentType().equals("application/pdf")){
            try{
                byte[] bytes = file.getBytes();
                Path path = Paths.get(helpPathReader.getPath() + "help.pdf");
                Files.write(path, bytes);
                return "redirect:adjustHelp?success";
            } catch (IOException exception){
                return "redirect:adjustHelp?failure";
            }
        }else{
            return "redirect:adjustHelp?failure";
        }

    }


    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
