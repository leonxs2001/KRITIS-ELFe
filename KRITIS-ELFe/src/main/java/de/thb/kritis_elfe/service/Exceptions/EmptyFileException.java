package de.thb.kritis_elfe.service.Exceptions;

public class EmptyFileException extends Exception{
    public EmptyFileException() {
        super("The file is empty.");
    }

    public EmptyFileException(String message) {
        super(message);
    }
}
