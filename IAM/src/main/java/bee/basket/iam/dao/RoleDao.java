package bee.basket.iam.dao;

import bee.basket.identityserver.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleDao extends JpaRepository<RoleEntity, UUID> {
    RoleEntity findByRole(String Role);
}
