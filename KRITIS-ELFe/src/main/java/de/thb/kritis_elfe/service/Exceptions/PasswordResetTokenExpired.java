package de.thb.kritis_elfe.service.Exceptions;

public class PasswordResetTokenExpired extends Exception {

    public PasswordResetTokenExpired(String msg){
        super(msg);
    }

    public PasswordResetTokenExpired(){super();}
}
