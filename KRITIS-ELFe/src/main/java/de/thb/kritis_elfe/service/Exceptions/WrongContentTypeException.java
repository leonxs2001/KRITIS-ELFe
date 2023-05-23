package de.thb.kritis_elfe.service.Exceptions;

public class WrongContentTypeException extends Exception{
    public WrongContentTypeException() {
        super("The file have the wrong content type.");
    }

    public WrongContentTypeException(String message) {
        super(message);
    }
}
