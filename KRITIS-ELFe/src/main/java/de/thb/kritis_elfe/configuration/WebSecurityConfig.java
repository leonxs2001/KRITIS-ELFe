package de.thb.kritis_elfe.configuration;


import de.thb.kritis_elfe.security.MyUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private MyUserDetailsService userDetailsService;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http//TODO change all paths und ordnen(zusammenfügen)
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .authorizeRequests()
                .antMatchers("/css/**", "/webjars/**", "/bootstrap/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .antMatchers("/", "/home", "/registrierung", "/bestaetigung/nutzer", "datenschutz").permitAll()
                .antMatchers("/hilfe", "/konto/**").access("isAuthenticated()")
                .antMatchers("/geschäftsstelle", "/bestätigung/geschäftsstelle/**").access("hasAuthority('ROLE_GESCHÄFTSSTELLE')")
                .antMatchers("/lagebericht/**").access("hasAnyAuthority('ROLE_LAND', 'ROLE_RESSORT', 'ROLE_BBK_ADMIN')")
                .antMatchers("/report/**").access("hasAnyAuthority('ROLE_BBK_ADMIN','ROLE_BBK_VIEWER')")
                .antMatchers("/report-kontrolle", "/report-details/**", "/ressorts", "/hilfe/bearbeiten").access("hasAuthority('ROLE_BBK_ADMIN')")
                .and()
                .formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .usernameParameter("username")
                .permitAll()
                .defaultSuccessUrl("/home")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/").permitAll();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
