package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.controller.form.ResetPasswordForm;
import de.thb.kritis_elfe.controller.form.ResetPasswordUserDataForm;
import de.thb.kritis_elfe.entity.PasswordResetToken;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.mail.EmailSender;
import de.thb.kritis_elfe.repository.PasswordResetTokenRepository;
import de.thb.kritis_elfe.service.Exceptions.EmailNotMatchingException;
import de.thb.kritis_elfe.service.Exceptions.PasswordNotMatchingException;
import de.thb.kritis_elfe.service.Exceptions.PasswordResetTokenExpired;
import de.thb.kritis_elfe.service.Exceptions.TokenAlreadyConfirmedException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class PasswordResetTokenService {

    private final UserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetToken getByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public PasswordResetToken getByUser(User user) {
        return passwordResetTokenRepository.findByUser(user);
    }

    public void deleteByExpiryDate(Date now) {
        passwordResetTokenRepository.deleteByExpiryDateLessThan(now);
    }

    public void deleteAllExpired(Date now) {
        passwordResetTokenRepository.deleteAllExpiredSince(now);
    }

    /**
     * Takes all Password reset token by expiry Date
     *
     * @param now as given Date
     * @return Stream PasswordResetToken
     */
    public Stream<PasswordResetToken> getAllByExpiryDate(Date now) {
        return passwordResetTokenRepository.findAllByExpiryDateLessThan(now);
    }

    /**
     * Creates new random Password Reset Token if user forgot his password
     *
     * @param user to link the right user
     * @throws EmailNotMatchingException
     */
    public void createPasswordResetToken(User user) throws EmailNotMatchingException {

        String token = UUID.randomUUID().toString();

        PasswordResetToken myToken = new PasswordResetToken(user, token);
        passwordResetTokenRepository.save(myToken);

        String link = "http://localhost:8080/reset-password?token=" + token;

        Context passwordResetContext = new Context();
        passwordResetContext.setVariable("username", user.getUsername());
        passwordResetContext.setVariable("link", link);
        emailSender.sendMailFromTemplate("/mail/reset_password", passwordResetContext, user.getEmail());
    }

    /**
     * activating password reset process
     *
     * @param token to get fitting connection user - token
     * @param form  to enter new password
     * @return true if process successful or false if process did not succeed
     * @throws PasswordResetTokenExpired
     */
    public void resetUserPassword(String token, ResetPasswordForm form) throws PasswordResetTokenExpired, PasswordNotMatchingException, TokenAlreadyConfirmedException {
        PasswordResetToken resetToken = getByToken(token);
        User user = resetToken.getUser();
        Date now = Date.from(Instant.now());

        if (!now.before(resetToken.getExpiryDate())) {
            throw new PasswordResetTokenExpired("Token has been expired.");
        }

        if(!form.getNewPassword().equals(form.getConfirmPassword())){
            throw new PasswordNotMatchingException("Both passwords has to be equal");
        }

        if(resetToken.isConfirmed()){
            throw new TokenAlreadyConfirmedException("The Token is already used.");
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        resetToken.setConfirmed(true);

        passwordResetTokenRepository.save(resetToken);
        userService.saveUser(user);
    }
}
