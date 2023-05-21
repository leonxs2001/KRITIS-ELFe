package de.thb.kritis_elfe.service.Exceptions;

public class WrongContentTypeException extends Exception{
    public WrongContentTypeException() {
        super();
    }

    public WrongContentTypeException(String message) {
        super(message);
    }
}
