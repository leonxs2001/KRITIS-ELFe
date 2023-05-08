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
        http
                .authorizeRequests()
                .antMatchers("/css/**", "/webjars/**", "/bootstrap/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .antMatchers("/", "/home", "/register/**", "/success_register", "/confirmation/confirmByUser/**", "datenschutz").permitAll()
                .antMatchers("/office").access("hasAuthority('ROLE_GESCHÄFTSSTELLE')")
                .antMatchers("/situation/**").access("hasAnyAuthority('ROLE_LAND', 'ROLE_RESSORT', 'ROLE_BBK_ADMIN')")
                .antMatchers("/report/**").access("hasAnyAuthority('ROLE_BBK_ADMIN','ROLE_BBK_VIEWER')")
                .antMatchers("/report-control/**").access("hasAuthority('ROLE_BBK_ADMIN')")
                .antMatchers("/report-details/**").access("hasAuthority('ROLE_BBK_ADMIN')")
                .antMatchers("/ressorts").access("hasAuthority('ROLE_BBK_ADMIN')")
                .antMatchers("/adjustHelp").access("hasAuthority('ROLE_BBK_ADMIN')")
                .antMatchers("/help").access("isAuthenticated()")
                .antMatchers("/account/user_details").access("isAuthenticated()")
                .antMatchers("/confirmation/confirm/**").access("hasAuthority('ROLE_GESCHÄFTSSTELLE')")
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
