package bee.basket.authserver.dao;

import bee.basket.authserver.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionDao extends JpaRepository<PermissionEntity,Long> {
    PermissionEntity findByPermission(String permission);
}
