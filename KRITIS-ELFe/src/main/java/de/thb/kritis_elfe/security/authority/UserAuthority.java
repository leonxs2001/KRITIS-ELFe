package de.thb.kritis_elfe.security.authority;

import org.springframework.security.core.GrantedAuthority;

public class UserAuthority implements GrantedAuthority {

    public final static String USER = "USER";

    @Override
    public String getAuthority() {
        return USER;
    }

}
