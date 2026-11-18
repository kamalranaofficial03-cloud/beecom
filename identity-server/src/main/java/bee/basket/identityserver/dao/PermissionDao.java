package bee.basket.identityserver.dao;

import bee.basket.identityserver.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionDao extends JpaRepository<PermissionEntity,Long> {
    PermissionEntity findByPermission(String permission);
}
