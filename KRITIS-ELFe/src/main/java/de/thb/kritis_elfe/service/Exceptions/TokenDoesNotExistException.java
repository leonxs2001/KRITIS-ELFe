package de.thb.kritis_elfe.service.Exceptions;

public class TokenDoesNotExistException extends Exception{
    public TokenDoesNotExistException() {
        super("The token does not exist.");
    }

    public TokenDoesNotExistException(String message) {
        super(message);
    }
}
