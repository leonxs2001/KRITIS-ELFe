package de.thb.kritis_elfe.service.Exceptions;

public class TokenDoesNotExistException extends Exception{
    public TokenDoesNotExistException() {
    }

    public TokenDoesNotExistException(String message) {
        super(message);
    }
}
