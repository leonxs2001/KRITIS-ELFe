package de.thb.kritis_elfe.service.Exceptions;

public class NotAuthorizedException extends Exception{
    public NotAuthorizedException(String msg){
        super(msg);
    }
    public NotAuthorizedException(){
        super();
    }
}
