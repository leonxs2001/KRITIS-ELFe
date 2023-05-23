package de.thb.kritis_elfe.service;

import de.thb.kritis_elfe.entity.ConfirmationToken;
import de.thb.kritis_elfe.entity.User;
import de.thb.kritis_elfe.repository.ConfirmationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {
    private ConfirmationTokenRepository confirmationTokenRepository;


    /**
     * Saving newly created confirmationtoken via CRUD Repository
     *
     * @param confirmationToken which is to be saved
     */
    public void saveConfirmationToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    /**
     * Getting whole Confirmationtoken Object by only searching for token
     *
     * @param token to be used for
     * @return found confirmationToken
     */
    public ConfirmationToken getConfirmationToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    /**
     * Setting user_confirmation to TRUE
     *
     * @param token to be taken as
     * @return return boolean as INTEGER value in DB (0 = false, 1 = true)
     */
    public int setConfirmedByUser(String token) {
        return confirmationTokenRepository.setConfirmedByUser(token);
    }

}
