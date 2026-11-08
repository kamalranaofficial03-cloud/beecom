package bee.basket.authserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class PermissionEntity extends Audit{
    @Column(name="permission",unique = true, nullable = false)
    private String permission;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
