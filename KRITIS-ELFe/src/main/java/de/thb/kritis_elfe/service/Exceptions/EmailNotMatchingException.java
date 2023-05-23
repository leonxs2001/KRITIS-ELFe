package de.thb.kritis_elfe.service.Exceptions;

import javax.naming.AuthenticationException;

public class EmailNotMatchingException extends AuthenticationException {

    public EmailNotMatchingException(){
        super("The emails do not match.");
    }

    public EmailNotMatchingException(String message){
        super(message);
    }
}
