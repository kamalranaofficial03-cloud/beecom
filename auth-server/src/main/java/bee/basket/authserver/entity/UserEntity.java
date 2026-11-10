package bee.basket.authserver.entity;

import bee.basket.commonutil.contract.ToModel;
import bee.basket.commonutil.model.User;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class UserEntity extends Audit implements ToModel<User> {
    @Column(name="username",unique = true, nullable = false)
    private String username;
    @Column(name="password",nullable = false)
    private String password;
    @Column(name="account_locked",nullable = false)
    private boolean accountLocked;
    @Column(name="enabled",nullable = false)
    private boolean enabled;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="users_roles",joinColumns = @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"
    ),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id",
                    referencedColumnName = "id"
            ))
    private Set<RoleEntity> role = new HashSet<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public Set<RoleEntity> getRole() {
        return role;
    }

    public void setRole(Set<RoleEntity> role) {
        this.role = role;
    }

    public User model(){
        return new User(this.username, password,enabled,new HashSet<>());
    }

    public static UserEntity create(User user){
        UserEntity newInstance = new UserEntity();
        newInstance.setPassword(user.password());
        newInstance.setUsername(user.username());
        newInstance.setEnabled(user.enabled());
        return newInstance;
    }
}
