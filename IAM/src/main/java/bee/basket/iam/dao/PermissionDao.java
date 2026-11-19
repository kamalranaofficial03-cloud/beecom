package bee.basket.iam.dao;

import bee.basket.iam.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionDao extends JpaRepository<PermissionEntity,Long> {
    PermissionEntity findByPermission(String permission);
}
