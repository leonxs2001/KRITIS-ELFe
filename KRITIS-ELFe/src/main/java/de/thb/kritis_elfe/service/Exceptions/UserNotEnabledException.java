package de.thb.kritis_elfe.service.Exceptions;

public class UserNotEnabledException extends RuntimeException{
    public UserNotEnabledException(String message){
        super(message);
    }
    public UserNotEnabledException(){
        super();
    }

}
