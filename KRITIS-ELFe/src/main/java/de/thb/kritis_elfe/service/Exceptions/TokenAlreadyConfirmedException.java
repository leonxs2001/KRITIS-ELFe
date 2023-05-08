package de.thb.kritis_elfe.service.Exceptions;

public class TokenAlreadyConfirmedException extends Exception{
    public TokenAlreadyConfirmedException() {
        super();
    }

    public TokenAlreadyConfirmedException(String message) {
        super(message);
    }
}
