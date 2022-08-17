package de.thb.webbaki.mail.confirmation;

import de.thb.webbaki.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
     * Showing when the confirmationtoken has been confirmed by User AND/OR Admin (Not sure how I`ll make it)
     *
     * @param token to be used for
     * @return DB-entry at table == confirmation_token, column == created_at
     */
    public int setConfirmedAt(String token) {
        return confirmationTokenRepository.setConfirmedAt(token, LocalDateTime.now());
    }

    /**
     * Using relation between confirmationtoken and user in case it is needed
     *
     * @param id is the referenced userId
     * @return matching user
     */
    public User getUserById(long id) {
        return confirmationTokenRepository.getUserById(id);
    }

    /**
     * Looking if user_confirmation is TRUE/FALSE in table confirmation_token
     *
     * @param token to look for
     * @return a boolean value
     */
    public boolean confirmedByUser(String token) {
        return confirmationTokenRepository.setConfirmedByUser(token) == 1;
    }

    /**
     * Looking if admin_confirmation is TRUE/FALSE in table confirmation_token
     *
     * @param token to look for
     * @return a boolean value
     */
    public boolean confirmedByAdmin(String token) {
        return confirmationTokenRepository.setConfirmedByAdmin(token) == 1;
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

    /**
     * Setting admin_confirmation to TRUE
     *
     * @param token to be taken as
     * @return return boolean as INTEGER value in DB (0 = false, 1 = true)
     */
    public int setConfirmedByAdmin(String token) {
        return confirmationTokenRepository.setConfirmedByAdmin(token);
    }

    public boolean accessGranted(String token){
        return this.confirmedByAdmin(token) && this.confirmedByUser(token);
    }

}
