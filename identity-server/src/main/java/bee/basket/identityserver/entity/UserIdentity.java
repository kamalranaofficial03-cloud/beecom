package bee.basket.identityserver.entity;

import bee.basket.commonutil.constant.IdentityProvider;
import jakarta.persistence.*;

@Entity
@Table(
        name = "user_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provider_provider_user",
                        columnNames = {
                                "provider",
                                "provider_user_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_provider_username",
                        columnNames = {
                                "provider",
                                "username"
                        }
                )
        }
)
public class UserIdentity extends Audit {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentityProvider provider;

    /**
     * Google sub
     * Microsoft oid
     * NULL for Local
     */
    @Column(name = "provider_user_id")
    private String providerUserId;

    /**
     * email or mobile
     */
    @Column(nullable = false)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "phone_verified")
    private Boolean phoneVerified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public IdentityProvider getProvider() {
        return provider;
    }

    public void setProvider(IdentityProvider provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
