package bee.basket.iam.service;

import bee.basket.commonutil.constant.IdentityProvider;
import bee.basket.iam.dao.UserIdentityDao;
import bee.basket.iam.entity.PermissionEntity;
import bee.basket.iam.entity.RoleEntity;
import bee.basket.iam.entity.UserEntity;
import bee.basket.iam.entity.UserIdentity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserIdentityDao userIdentityDao;

    @Test
    void loadUserCreatesDetailsFromIdentityAndCombinesDistinctRolesAndPermissions() {
        UserEntity user = new UserEntity();
        user.setEnabled(true);
        user.setRole(Set.of(
                role("ROLE_admin", Set.of(permission("read"))),
                role("user", Set.of(permission("read"), permission("write")))
        ));
        UserIdentity identity = identity("ada", "encoded-password", true, user);
        when(userIdentityDao.findByProviderAndUsername(IdentityProvider.LOCAL, "ada"))
                .thenReturn(Optional.of(identity));

        var details = new CustomUserDetailsService(userIdentityDao).loadUserByUsername("ada");

        assertThat(details.getUsername()).isEqualTo("ada");
        assertThat(details.getPassword()).isEqualTo("encoded-password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER", "read", "write");
        verify(userIdentityDao).findByProviderAndUsername(IdentityProvider.LOCAL, "ada");
    }

    @Test
    void loadUserThrowsWhenIdentityDoesNotExist() {
        when(userIdentityDao.findByProviderAndUsername(IdentityProvider.LOCAL, "missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CustomUserDetailsService(userIdentityDao).loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loadUserThrowsWhenIdentityIsNotLinkedToAUser() {
        UserIdentity identity = identity("unlinked", "hash", true, null);
        when(userIdentityDao.findByProviderAndUsername(IdentityProvider.LOCAL, "unlinked"))
                .thenReturn(Optional.of(identity));

        assertThatThrownBy(() -> new CustomUserDetailsService(userIdentityDao).loadUserByUsername("unlinked"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void loadUserDisablesDetailsWhenIdentityIsDisabled() {
        UserEntity user = new UserEntity();
        user.setEnabled(true);
        UserIdentity identity = identity("disabled-identity", "hash", false, user);
        when(userIdentityDao.findByProviderAndUsername(IdentityProvider.LOCAL, "disabled-identity"))
                .thenReturn(Optional.of(identity));

        var details = new CustomUserDetailsService(userIdentityDao).loadUserByUsername("disabled-identity");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserDisablesDetailsWhenUserIsDisabled() {
        UserEntity user = new UserEntity();
        user.setEnabled(false);
        UserIdentity identity = identity("disabled-user", "hash", true, user);
        when(userIdentityDao.findByProviderAndUsername(IdentityProvider.LOCAL, "disabled-user"))
                .thenReturn(Optional.of(identity));

        var details = new CustomUserDetailsService(userIdentityDao).loadUserByUsername("disabled-user");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserHandlesRolesWithoutPermissions() {
        UserEntity user = new UserEntity();
        user.setEnabled(true);
        user.setRole(Set.of(role("guest", Set.of())));
        UserIdentity identity = identity("guest", "hash", true, user);
        when(userIdentityDao.findByProviderAndUsername(IdentityProvider.LOCAL, "guest"))
                .thenReturn(Optional.of(identity));

        var details = new CustomUserDetailsService(userIdentityDao).loadUserByUsername("guest");

        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_GUEST");
    }

    private static UserIdentity identity(String username, String password, boolean enabled, UserEntity user) {
        UserIdentity identity = new UserIdentity();
        identity.setProvider(IdentityProvider.LOCAL);
        identity.setUsername(username);
        identity.setPasswordHash(password);
        identity.setEnabled(enabled);
        identity.setUser(user);
        return identity;
    }

    private static RoleEntity role(String roleName, Set<PermissionEntity> permissions) {
        RoleEntity role = new RoleEntity();
        role.setRole(roleName);
        role.setPermissions(permissions);
        return role;
    }

    private static PermissionEntity permission(String permissionName) {
        PermissionEntity permission = new PermissionEntity();
        permission.setPermission(permissionName);
        return permission;
    }
}
