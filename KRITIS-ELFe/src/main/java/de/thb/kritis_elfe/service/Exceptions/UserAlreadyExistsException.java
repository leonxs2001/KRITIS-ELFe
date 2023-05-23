package de.thb.kritis_elfe.service.Exceptions;

import javax.naming.AuthenticationException;

public class UserAlreadyExistsException extends AuthenticationException {
    public UserAlreadyExistsException(final String msg){
        super(msg);
    }
    public UserAlreadyExistsException(){
        super("The User already exist.");
    }
}
