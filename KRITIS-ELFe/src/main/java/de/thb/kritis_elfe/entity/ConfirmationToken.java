package de.thb.kritis_elfe.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ConfirmationToken {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime confirmedAt;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Boolean userConfirmation = false;

    @Column(nullable = false)
    private Boolean adminConfirmation = false;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id")
    private User user;

    public boolean accessGranted() {
        if (this.adminConfirmation && this.userConfirmation) {
            return true;
        }else {
            return false;
        }
    }

    public ConfirmationToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, User user) {
        setToken(token);
        setCreatedAt(createdAt);
        setExpiresAt(expiresAt);
        setUser(user);
    }
}
