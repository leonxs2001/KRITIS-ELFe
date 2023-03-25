package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.controller.form.ChangeCredentialsForm;
import de.thb.kritis_elfe.controller.form.UserFormModel;
import de.thb.kritis_elfe.controller.form.UserRegisterFormModel;
import de.thb.kritis_elfe.controller.form.UserToRoleFormModel;
import de.thb.kritis_elfe.entity.Role;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.mail.EmailSender;
import de.thb.kritis_elfe.mail.Templates.AdminNotifications.*;
import de.thb.kritis_elfe.mail.Templates.UserNotifications.*;
import de.thb.kritis_elfe.entity.ConfirmationToken;
import de.thb.kritis_elfe.repository.RoleRepository;
import de.thb.kritis_elfe.repository.UserRepository;
import de.thb.kritis_elfe.service.Exceptions.EmailNotMatchingException;
import de.thb.kritis_elfe.service.Exceptions.PasswordNotMatchingException;
import de.thb.kritis_elfe.service.Exceptions.UserAlreadyExistsException;
import de.thb.kritis_elfe.service.questionnaire.QuestionnaireService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;

import javax.transaction.Transactional;
import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@Service
@Transactional
@Slf4j
public class UserService {
    private UserRepository userRepository; ////initialize repository Object
    private RoleRepository roleRepository;
    private RoleService roleService;
    private PasswordEncoder passwordEncoder;
    private ConfirmationTokenService confirmationTokenService;
    private EmailSender emailSender;
    private BranchService branchService;
    private QuestionnaireService questionnaireService;

    //Repo Methods --------------------------
    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getById(long id){return userRepository.findById(id);}
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Boolean usernameExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUserByAdminRole() {
        return userRepository.findByRoles_Name("ROLE_BBK_ADMIN");
    }

    public User createUser(User user){
        return userRepository.save(user);
    }

    /**
     * @param user is used to create new user -> forwarded to registerNewUser
     * @return newly created token
     */
    public String createToken(User user) {

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token, LocalDateTime.now(), LocalDateTime.now().plusDays(3), user);

        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    /**
     * Registering new User with all parameters from User.java
     * Using emailExists() to check whether user already exists
     */
    public void registerNewUser(final UserRegisterFormModel form) throws UserAlreadyExistsException {
        if (usernameExists(form.getUsername())) {
            throw new UserAlreadyExistsException("Es existiert bereits ein Account mit folgender Email-Adresse: " + form.getEmail());
        } else {

            User user = User.builder().
                    lastName(form.getLastname()).
                    firstName(form.getFirstname()).
                    username(form.getUsername()).
                    email(form.getEmail()).
                    enabled(false).
                    password(passwordEncoder.encode(form.getPassword())).
                    roles(Collections.singletonList(form.getRole())).build();

            if(form.getRole().getName() == "ROLE_LAND"){
                user.setFederalState(form.getFederalState());
            }else if(form.getRole().getName() == "ROLE_RESSORT"){
                user.setRessort(form.getRessort());
            }

            String token = createToken(user); // To create the token of the user


            String userLink = "https://kritis-elfe.th-brandenburg.de/confirmation/confirmByUser?token=" + token;

            userRepository.save(user);

            /*Outsourcing Mail to thread for speed purposes*/
            new Thread(() -> {
                //Email to new registered user
                emailSender.send(form.getEmail(), UserRegisterNotification.buildUserEmail(form.getFirstname(), form.getLastname(), userLink));
            }).start();

        }
    }

    /**
     * Confirm created userconfirmation token
     * @param token
     * @return
     * @throws IllegalStateException
     */
    @Transactional
    public void confirmTokenByAdmin(String token) throws IllegalStateException {

        ConfirmationToken confirmationToken = confirmationTokenService.getConfirmationToken(token);
        User user = confirmationToken.getUser();

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        } else {
            confirmationToken.setConfirmedAt(LocalDateTime.now());
            confirmationToken.setAdminConfirmation(true);
            confirmationTokenService.saveConfirmationToken(confirmationToken);
        }
        if (confirmationToken.getUserConfirmation()) {
            user.setEnabled(true);
            userRepository.save(user);
        }
        new Thread(() -> {
            emailSender.send(user.getEmail(), UserEnabledNotification.finalEnabledConfirmation(user.getFirstName(), user.getLastName()));
        }).start();

    }

    /**
     * Setting user_confirmation TRUE or False
     *
     * @param token to get matching ConfirmationToken
     * @return value TRUE or FALSE based on bit value (0 = false, 1 = true)
     */
    public int userConfirmation(String token) {
        return confirmationTokenService.setConfirmedByUser(token);
    }

    /**
     * Setting user_confirmation TRUE or False and getting HTML Page with confirmation Details
     * using method public int userConfirmation(String token)
     *
     * @param token to get matching ConfirmationToken
     */
    public void confirmUser(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService.getConfirmationToken(token);
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        } else {
            //The confirmation only should be done if it is not already done
            if (!confirmationToken.getUserConfirmation()) {
                userConfirmation(token);

                //send link to admin
                String adminLink = "https://kritis-elfe.th-brandenburg.de/confirmation/confirm?token=" + token;
                User user = confirmationToken.getUser();

                /* Outsourcing Mailsending to thread for speed purposes */
                new Thread(() -> {

                    emailSender.send(user.getEmail(), UserNotificationAfterUserConfirmation.mailAfterUserConfirm(user.getFirstName(), user.getLastName()));

                    for (User adminUser : getUserByAdminRole()) {
                        //only send it to enabled users
                        if(adminUser.isEnabled()) {
                            emailSender.send(adminUser.getEmail(), AdminRegisterNotification.buildAdminEmail(adminUser.getFirstName(), adminLink,
                                    user.getFirstName(), user.getLastName(),
                                    user.getEmail(), user.getRoles()));
                        }
                    }
                }).start();
            }
        }
    }



    public void setCurrentLogin(User u) {
        u.setLastLogin(LocalDateTime.now());
        userRepository.save(u);
    }

    /**
     * USED IN SUPERADMIN DASHBOARD
     * Superadmin can add Roles to specific Users
     *
     * @param userToRoleFormModel to get Userdata, especially roles from user
     */
    public void addAndDeleteRoles(UserToRoleFormModel userToRoleFormModel) {

        for (int i = 0; i < userToRoleFormModel.getUsers().size(); i++) {

            User user = getUserByUsername(userToRoleFormModel.getUsers().get(i).getUsername());

            String roleString = userToRoleFormModel.getRole().get(i);
            String roleDelString = userToRoleFormModel.getRoleDel().get(i);

            if (!roleString.equals("none")) {
                Role role = roleService.getRoleByName(roleString);
                //only add a role to a person, if he not already has this role
                if (!user.getRoles().contains(role)) {
                    user.getRoles().add(role);

                    //create new questionnaires for the user if he is now KRITIS_BETREIBER and hasnt already one
                    if (role.getName().equals("ROLE_KRITIS_BETREIBER") /*&& !questionnaireService.existsByUserId(user.getId())*/) {
                        questionnaireService.createQuestionnaireForUser(user);
                    }

                    /*Outsourcing Mail to thread for speed purposes*/
                    new Thread(() -> {
                        /*for (User superAdmin : getUserByAdminrole()) {
                            //only send it to enabled users
                            if(superAdmin.isEnabled()) {
                                emailSender.send(superAdmin.getEmail(), AdminAddRoleNotification.changeRole(superAdmin.getFirstName(),
                                        superAdmin.getLastName(),
                                        role, user.getUsername()));
                            }
                        }*/
                        emailSender.send(user.getEmail(), UserAddRoleNotification.changeRoleMail(user.getFirstName(),
                                user.getLastName(),
                                role));
                    }).start();
                }
            }

            if (!roleDelString.equals("none")) {
                Role roleDel = roleService.getRoleByName(roleDelString);
                user.getRoles().remove(roleDel);

                /*Outsourcing Mail to thread for speed purposes*/
                new Thread(() -> {
                    emailSender.send(user.getEmail(), UserRemoveRoleNotification.removeRoleMail(user.getFirstName(),
                            user.getLastName(),
                            roleDel));

                    /*for (User superAdmin : getUserByAdminrole()) {
                        //only send it to enabled users
                        if(superAdmin.isEnabled()) {
                            emailSender.send(superAdmin.getEmail(), AdminRemoveRoleNotification.removeRole(superAdmin.getFirstName(),
                                    superAdmin.getLastName(),
                                    roleDel,
                                    user.getUsername()));
                        }
                    }*/
                }).start();
            }

            if (!roleDelString.equals("none") || !roleString.equals("none")) {
                saveUser(user);
            }

        }
    }


    /**
     * Enable/Disable user to give access to KRITIS-ELFe
     *
     * @param form to get userlist
     */
    public void changeEnabledStatus(UserFormModel form) {

        List<User> users = getAllUsers();

        /*Outsourcing Mail to thread for speed purposes*/
        new Thread(() -> {
            for (int i = 0; i < users.size(); i++) {

                if (users.get(i).isEnabled() != (form.getUsers().get(i).isEnabled())) {
                    users.get(i).setEnabled(form.getUsers().get(i).isEnabled());


                    emailSender.send(users.get(i).getEmail(), UserChangeEnabledStatusNotification.changeBrancheMail(users.get(i).getFirstName(), users.get(i).getLastName()));

                    for (User officeAdmin : getUserByAdminRole()) {
                        //only send it to enabled users
                        if(officeAdmin.isEnabled()) {
                            emailSender.send(officeAdmin.getEmail(), AdminDeactivateUserSubmit.changeEnabledStatus(officeAdmin.getFirstName(),
                                    officeAdmin.getLastName(),
                                    users.get(i).isEnabled(),
                                    users.get(i).getUsername()));
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * Let user change the own credentials: password, firstname, lastname, email
     * @param form
     * @param user
     * @param model
     * @throws PasswordNotMatchingException
     * @throws EmailNotMatchingException
     */
    public void changeCredentials(ChangeCredentialsForm form, User user, Model model) throws PasswordNotMatchingException, EmailNotMatchingException {

        if (form.getOldPassword() != null) {
            if (!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
                throw new PasswordNotMatchingException("Das eingegebene Passwort stimmt nicht mit Ihrem aktuellen Passwort überein.");
            } else if (!form.getOldPassword().equals(form.getNewPassword()) && form.getNewPassword().equals(form.getConfirmNewPassword())) {
                user.setPassword(passwordEncoder.encode(form.getNewPassword()));
                model.addAttribute("passwordSuccess", "Ihr Passwort wurde erfolgreich geändert.");
            }
        }

        if (form.getOldEmail() != null) {
            if (!form.getOldEmail().equals(user.getEmail())) {
                throw new EmailNotMatchingException("Die eingegebene Email-Adresse stimmt nicht mit Ihrer Email überein.");
            }
            else if (!form.getOldEmail().equals(form.getNewEmail())) {
                user.setEmail(form.getNewEmail());
                model.addAttribute("emailSuccess", "Ihre Email-Adresse wurde erfolgreich geändert.");
            }
        }

        if (form.getNewFirstname() != null && !form.getNewFirstname().isEmpty()) {
            if (!form.getNewFirstname().equals(user.getFirstName())) {
                user.setFirstName(form.getNewFirstname());
                model.addAttribute("firstnameSuccess", "Ihr Vorname wurde erfolgreich geändert.");
            }
        }

        if (form.getNewLastname() != null && !form.getNewLastname().isEmpty()) {
            if (!form.getNewLastname().equals(user.getLastName())) {
                user.setLastName(form.getNewLastname());
                model.addAttribute("lastnameSuccess", "Ihr Nachname wurde erfolgreich geändert.");
            }
        }

        saveUser(user);
    }
}
