package de.thb.kritis_elfe.mail;

import org.thymeleaf.context.Context;

public interface EmailSender {
    void send(String to, String email);

    void sendMailFromTemplate(String template, Context context, String mailAddress);
}
