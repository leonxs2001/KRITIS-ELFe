package de.thb.kritis_elfe.service.Exceptions;

public class EntityDoesNotExistException extends Exception{
    public  EntityDoesNotExistException(String message){
        super(message);
    }

    public EntityDoesNotExistException(){
        super("The entity does not exist.");
    }
}
