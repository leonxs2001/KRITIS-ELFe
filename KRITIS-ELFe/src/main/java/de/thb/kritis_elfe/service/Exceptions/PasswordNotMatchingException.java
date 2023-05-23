package de.thb.kritis_elfe.service.Exceptions;

import javax.naming.AuthenticationException;

public class PasswordNotMatchingException extends AuthenticationException {

    public PasswordNotMatchingException(){
        super("The passwords do not match.");
    }

    public PasswordNotMatchingException(String message){
        super(message);
    }
}
