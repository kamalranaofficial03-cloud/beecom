package bee.basket.authserver.dao;

import bee.basket.authserver.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<RoleEntity, Long> {
    RoleEntity findByRole(String Role);
}
