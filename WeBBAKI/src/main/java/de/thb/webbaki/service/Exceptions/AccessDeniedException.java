package de.thb.webbaki.service.Exceptions;

public class AccessDeniedException extends Exception{
    public AccessDeniedException(String message){
        super(message);
    }

    public AccessDeniedException(){
        super();
    }
}
