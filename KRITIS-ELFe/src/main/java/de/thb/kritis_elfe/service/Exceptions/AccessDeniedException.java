package de.thb.kritis_elfe.service.Exceptions;

public class AccessDeniedException extends Exception{
    public AccessDeniedException(String message){
        super(message);
    }

    public AccessDeniedException(){
        super();
    }
}
