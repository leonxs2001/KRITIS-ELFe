package de.thb.kritis_elfe.configuration;


import de.thb.kritis_elfe.repository.RoleRepository;
import de.thb.kritis_elfe.repository.UserRepository;
import de.thb.kritis_elfe.security.MyUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@AllArgsConstructor
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Bean
    public MyUserDetailsService userDetailsService() {
        return new MyUserDetailsService(userRepository, roleRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
