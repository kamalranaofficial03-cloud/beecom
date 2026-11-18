package bee.basket.identityserver.dao;

import bee.basket.identityserver.entity.UserIdentity;
import bee.basket.commonutil.constant.IdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityDao extends JpaRepository<UserIdentity, UUID> {
    Optional<UserIdentity>
    findByProviderAndUsername(
            IdentityProvider provider,
            String username
    );

    Optional<UserIdentity>
    findByProviderAndProviderUserId(
            IdentityProvider provider,
            String providerUserId
    );

    boolean existsByProviderAndUsername(
            IdentityProvider provider,
            String username
    );

    boolean existsByUsername(String username);
}
