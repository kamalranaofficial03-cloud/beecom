package bee.basket.iam.dao;


import bee.basket.iam.entity.UserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

@ConditionalOnProperty(prefix = "app.datasource", name = "provider", havingValue = "rd", matchIfMissing = false)
public interface UserDao extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}
