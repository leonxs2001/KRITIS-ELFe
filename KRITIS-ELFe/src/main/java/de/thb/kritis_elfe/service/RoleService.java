package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.Role;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.repository.RoleRepository;
import de.thb.kritis_elfe.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<Role> getAllRoles() {
        return (List<Role>) roleRepository.findAll();
    }

    public Role getRoleByName(String rolename){return roleRepository.findByName(rolename);}

    public Role createRole(Role role){return roleRepository.save(role);}

    public Collection<Role> getRolesByUser(String email) {
        User user = userRepository.findByEmail(email);

        return user.getRoles();
    }

    public void addRoleToUser(String email, String roleName) {
        User user = userRepository.findByEmail(email);
        Role role = roleRepository.findByName(roleName);

        user.getRoles().add(role);
    }
}
