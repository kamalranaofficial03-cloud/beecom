package bee.basket.iam.entity;

import bee.basket.commonutil.constant.IdentityProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityModelTest {

    @Test
    void baseIdReturnsNullUntilIdIsAssignedAndThenReturnsAssignedId() throws Exception {
        UserEntity user = new UserEntity();
        assertThat(user.getId()).isNull();

        UUID id = UUID.randomUUID();
        Field idField = BaseId.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);

        assertThat(user.getId()).isEqualTo(id);
        assertThat(idField.getAnnotation(Id.class)).isNotNull();
        assertThat(idField.getAnnotation(GeneratedValue.class).strategy()).isEqualTo(GenerationType.UUID);
    }

    @Test
    void permissionEntityStoresAndClearsPermission() {
        PermissionEntity permission = new PermissionEntity();

        assertThat(permission.getPermission()).isNull();
        permission.setPermission("read");
        assertThat(permission.getPermission()).isEqualTo("read");
        permission.setPermission(null);
        assertThat(permission.getPermission()).isNull();
        assertThat(PermissionEntity.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(PermissionEntity.class.getAnnotation(Table.class).name()).isEqualTo("permissions");
    }

    @Test
    void roleEntityStoresRoleAndPermissions() {
        RoleEntity role = new RoleEntity();
        PermissionEntity permission = new PermissionEntity();
        permission.setPermission("read");

        assertThat(role.getRole()).isNull();
        assertThat(role.getPermissions()).isEmpty();
        role.setRole("user");
        role.setPermissions(Set.of(permission));

        assertThat(role.getRole()).isEqualTo("user");
        assertThat(role.getPermissions()).containsExactly(permission);
        assertThat(RoleEntity.class.getAnnotation(Table.class).name()).isEqualTo("roles");
        assertThat(column(RoleEntity.class, "role").unique()).isTrue();
        assertThat(column(RoleEntity.class, "role").nullable()).isFalse();
    }

    @Test
    void rolePermissionsMappingIsEagerAndUsesExpectedJoinColumns() throws Exception {
        Field field = RoleEntity.class.getDeclaredField("permissions");

        assertThat(field.getAnnotation(ManyToMany.class).fetch()).isEqualTo(FetchType.EAGER);
        JoinTable joinTable = field.getAnnotation(JoinTable.class);
        assertThat(joinTable.name()).isEqualTo("roles_permissions");
        assertThat(joinTable.joinColumns()[0].name()).isEqualTo("role_id");
        assertThat(joinTable.joinColumns()[0].referencedColumnName()).isEqualTo("id");
        assertThat(joinTable.inverseJoinColumns()[0].name()).isEqualTo("permission_id");
        assertThat(joinTable.inverseJoinColumns()[0].referencedColumnName()).isEqualTo("id");
    }

    @Test
    void userEntityStoresProfileAndEnabledState() {
        UserEntity user = new UserEntity();

        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.isEnabled()).isFalse();
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setEmail("ada@example.com");
        user.setEnabled(true);

        assertThat(user.getFirstName()).isEqualTo("Ada");
        assertThat(user.getLastName()).isEqualTo("Lovelace");
        assertThat(user.getEmail()).isEqualTo("ada@example.com");
        assertThat(user.isEnabled()).isTrue();
        user.setEnabled(false);
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void userEntityInitializesIdentityAndRoleCollectionsAndAllowsReplacingThem() {
        UserEntity user = new UserEntity();

        assertThat(user.getIdentities()).isEmpty();
        assertThat(user.getRole()).isEmpty();

        List<UserIdentity> identities = new ArrayList<>();
        Set<RoleEntity> roles = new HashSet<>();
        user.setIdentities(identities);
        user.setRole(roles);

        assertThat(user.getIdentities()).isSameAs(identities);
        assertThat(user.getRole()).isSameAs(roles);
    }

    @Test
    void addIdentityAddsIdentityAndSetsItsUser() {
        UserEntity user = new UserEntity();
        UserIdentity identity = new UserIdentity();

        user.addIdentity(identity);

        assertThat(user.getIdentities()).containsExactly(identity);
        assertThat(identity.getUser()).isSameAs(user);
    }

    @Test
    void userEntityMapsEmailUniquenessAndIdentityAndRoleRelationships() throws Exception {
        Table table = UserEntity.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("users");
        assertThat(table.uniqueConstraints()).extracting(UniqueConstraint::name)
                .contains("uk_users_email");

        Field identities = UserEntity.class.getDeclaredField("identities");
        OneToMany oneToMany = identities.getAnnotation(OneToMany.class);
        assertThat(oneToMany.mappedBy()).isEqualTo("user");
        assertThat(oneToMany.cascade()).contains(jakarta.persistence.CascadeType.ALL);
        assertThat(oneToMany.orphanRemoval()).isTrue();

        Field roles = UserEntity.class.getDeclaredField("role");
        assertThat(roles.getAnnotation(ManyToMany.class).fetch()).isEqualTo(FetchType.EAGER);
        JoinTable joinTable = roles.getAnnotation(JoinTable.class);
        assertThat(joinTable.name()).isEqualTo("users_roles");
        assertThat(joinTable.joinColumns()[0].name()).isEqualTo("user_id");
        assertThat(joinTable.inverseJoinColumns()[0].name()).isEqualTo("role_id");
    }

    @Test
    void userIdentityStoresProviderAndProviderUserId() {
        UserIdentity identity = new UserIdentity();

        assertThat(identity.getProvider()).isNull();
        assertThat(identity.getProviderUserId()).isNull();
        identity.setProvider(IdentityProvider.GOOGLE);
        identity.setProviderUserId("google-sub");

        assertThat(identity.getProvider()).isEqualTo(IdentityProvider.GOOGLE);
        assertThat(identity.getProviderUserId()).isEqualTo("google-sub");
        identity.setProvider(null);
        identity.setProviderUserId(null);
        assertThat(identity.getProvider()).isNull();
        assertThat(identity.getProviderUserId()).isNull();
    }

    @Test
    void userIdentityStoresCredentialsAndUsername() {
        UserIdentity identity = new UserIdentity();

        assertThat(identity.getUsername()).isNull();
        assertThat(identity.getPasswordHash()).isNull();
        identity.setUsername("ada@example.com");
        identity.setPasswordHash("encoded");

        assertThat(identity.getUsername()).isEqualTo("ada@example.com");
        assertThat(identity.getPasswordHash()).isEqualTo("encoded");
        identity.setUsername(null);
        identity.setPasswordHash(null);
        assertThat(identity.getUsername()).isNull();
        assertThat(identity.getPasswordHash()).isNull();
    }

    @Test
    void userIdentityStoresEnablementAndVerificationStates() {
        UserIdentity identity = new UserIdentity();

        assertThat(identity.isEnabled()).isTrue();
        assertThat(identity.getEmailVerified()).isNull();
        assertThat(identity.getPhoneVerified()).isNull();
        identity.setEnabled(false);
        identity.setEmailVerified(true);
        identity.setPhoneVerified(false);

        assertThat(identity.isEnabled()).isFalse();
        assertThat(identity.getEmailVerified()).isTrue();
        assertThat(identity.getPhoneVerified()).isFalse();

        identity.setEnabled(true);
        identity.setEmailVerified(null);
        identity.setPhoneVerified(null);
        assertThat(identity.isEnabled()).isTrue();
        assertThat(identity.getEmailVerified()).isNull();
        assertThat(identity.getPhoneVerified()).isNull();
    }

    @Test
    void userIdentityCanSetAndClearItsAssociatedUser() {
        UserIdentity identity = new UserIdentity();
        UserEntity user = new UserEntity();

        identity.setUser(user);
        assertThat(identity.getUser()).isSameAs(user);
        identity.setUser(null);
        assertThat(identity.getUser()).isNull();
    }

    @Test
    void userIdentityMapsConstraintsAndAssociations() throws Exception {
        Table table = UserIdentity.class.getAnnotation(Table.class);
        assertThat(table.name()).isEqualTo("user_identities");
        assertThat(table.uniqueConstraints()).extracting(UniqueConstraint::name)
                .containsExactlyInAnyOrder("uk_provider_provider_user", "uk_provider_username");

        Field provider = UserIdentity.class.getDeclaredField("provider");
        assertThat(provider.getAnnotation(jakarta.persistence.Enumerated.class).value())
                .isEqualTo(jakarta.persistence.EnumType.STRING);
        assertThat(column(UserIdentity.class, "provider").nullable()).isFalse();
        assertThat(column(UserIdentity.class, "username").nullable()).isFalse();
        assertThat(column(UserIdentity.class, "enabled").nullable()).isFalse();
        assertThat(column(UserIdentity.class, "providerUserId").name()).isEqualTo("provider_user_id");
        assertThat(column(UserIdentity.class, "passwordHash").name()).isEqualTo("password_hash");

        Field user = UserIdentity.class.getDeclaredField("user");
        assertThat(user.getAnnotation(ManyToOne.class).fetch()).isEqualTo(FetchType.LAZY);
        assertThat(user.getAnnotation(JoinColumn.class).name()).isEqualTo("user_id");
    }

    private static Column column(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName).getAnnotation(Column.class);
        } catch (NoSuchFieldException exception) {
            throw new AssertionError(exception);
        }
    }
}
