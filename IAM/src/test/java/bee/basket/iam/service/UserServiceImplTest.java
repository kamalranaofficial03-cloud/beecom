package bee.basket.iam.service;

import bee.basket.commonutil.constant.IdentityProvider;
import bee.basket.commonutil.constant.RoleEnum;
import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.request.RegisterRequest;
import bee.basket.iam.dao.RoleDao;
import bee.basket.iam.dao.UserDao;
import bee.basket.iam.dao.UserIdentityDao;
import bee.basket.iam.entity.PermissionEntity;
import bee.basket.iam.entity.RoleEntity;
import bee.basket.iam.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private UserIdentityDao userIdentityDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleDao roleDao;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userDao, userIdentityDao, passwordEncoder, roleDao);
    }

    @Test
    void registerCreatesUserIdentityAndReturnsRolePermissions() throws Exception {
        RegisterRequest request = request();
        RoleEntity userRole = role("user", Set.of(permission("read"), permission("write")));
        when(userDao.existsByEmail(request.email())).thenReturn(false);
        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, request.username())).thenReturn(false);
        when(roleDao.findByRole(RoleEnum.USER.value())).thenReturn(userRole);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userDao.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            setId(user, UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            return user;
        });

        var result = service.register(request);

        assertThat(result.id()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(result.firstName()).isEqualTo(request.firstName());
        assertThat(result.lastName()).isEqualTo(request.lastName());
        assertThat(result.email()).isEqualTo(request.email());
        assertThat(result.enabled()).isTrue();
        assertThat(result.roles()).singleElement().satisfies(resultRole -> {
            assertThat(resultRole.role()).isEqualTo("user");
            assertThat(resultRole.permissions()).extracting("permission")
                    .containsExactlyInAnyOrder("read", "write");
        });

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userDao).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).containsExactly(userRole);
        assertThat(savedUser.getIdentities()).singleElement().satisfies(identity -> {
            assertThat(identity.getProvider()).isEqualTo(IdentityProvider.LOCAL);
            assertThat(identity.getUsername()).isEqualTo(request.username());
            assertThat(identity.getPasswordHash()).isEqualTo("encoded-password");
            assertThat(identity.isEnabled()).isTrue();
            assertThat(identity.getUser()).isSameAs(savedUser);
        });
    }

    @Test
    void registerThrowsWhenEmailAlreadyExistsWithoutCheckingUsernameOrSaving() {
        RegisterRequest request = request();
        when(userDao.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(UsernameAlreadyExistException.class)
                .hasMessage("Email already exists");

        verify(userIdentityDao, never()).existsByProviderAndUsername(any(), any());
        verify(roleDao, never()).findByRole(any());
        verify(userDao, never()).save(any());
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExistsWithoutLookingUpRole() {
        RegisterRequest request = request();
        when(userDao.existsByEmail(request.email())).thenReturn(false);
        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, request.username()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(UsernameAlreadyExistException.class)
                .hasMessage("Username already exists");

        verify(roleDao, never()).findByRole(any());
        verify(userDao, never()).save(any());
    }

    @Test
    void registerThrowsWhenDefaultUserRoleIsMissing() {
        RegisterRequest request = request();
        when(userDao.existsByEmail(request.email())).thenReturn(false);
        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, request.username()))
                .thenReturn(false);
        when(roleDao.findByRole(RoleEnum.USER.value())).thenReturn(null);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The default user role is not configured");

        verify(passwordEncoder, never()).encode(any());
        verify(userDao, never()).save(any());
    }

    @Test
    void registerPropagatesPasswordEncoderFailure() {
        RegisterRequest request = request();
        when(userDao.existsByEmail(request.email())).thenReturn(false);
        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, request.username()))
                .thenReturn(false);
        when(roleDao.findByRole(RoleEnum.USER.value())).thenReturn(role("user", Set.of()));
        when(passwordEncoder.encode(request.password())).thenThrow(new IllegalArgumentException("encoder failed"));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("encoder failed");

        verify(userDao, never()).save(any());
    }

    @Test
    void registerPropagatesPersistenceFailure() {
        RegisterRequest request = request();
        when(userDao.existsByEmail(request.email())).thenReturn(false);
        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, request.username()))
                .thenReturn(false);
        when(roleDao.findByRole(RoleEnum.USER.value())).thenReturn(role("user", Set.of()));
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userDao.save(any(UserEntity.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("database unavailable");
    }

    @Test
    void checkIfExistQueriesOnlyLocalIdentity() {
        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, "ada"))
                .thenReturn(true);

        assertThat(service.checkIfExist("ada")).isTrue();

        when(userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, "grace"))
                .thenReturn(false);
        assertThat(service.checkIfExist("grace")).isFalse();
    }

    private static RegisterRequest request() {
        return new RegisterRequest("Ada", "Lovelace", "ada@example.com", "ada", "secret");
    }

    private static RoleEntity role(String name, Set<PermissionEntity> permissions) {
        RoleEntity role = new RoleEntity();
        role.setRole(name);
        role.setPermissions(permissions);
        return role;
    }

    private static PermissionEntity permission(String name) {
        PermissionEntity permission = new PermissionEntity();
        permission.setPermission(name);
        return permission;
    }

    private static void setId(UserEntity user, UUID id) {
        try {
            Field idField = UserEntity.class.getSuperclass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
