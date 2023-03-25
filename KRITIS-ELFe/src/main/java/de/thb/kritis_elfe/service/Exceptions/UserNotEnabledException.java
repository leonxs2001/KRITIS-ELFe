package de.thb.kritis_elfe.service.Exceptions;

public class UserNotEnabledException extends Exception{
    public UserNotEnabledException(String message){
        super(message);
    }
    public UserNotEnabledException(){
        super();
    }

}
