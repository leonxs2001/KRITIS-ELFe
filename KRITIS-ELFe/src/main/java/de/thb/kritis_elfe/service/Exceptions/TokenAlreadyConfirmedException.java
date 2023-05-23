package de.thb.kritis_elfe.service.Exceptions;

public class TokenAlreadyConfirmedException extends Exception{
    public TokenAlreadyConfirmedException() {
        super("The token is already confirmed.");
    }

    public TokenAlreadyConfirmedException(String message) {
        super(message);
    }
}
