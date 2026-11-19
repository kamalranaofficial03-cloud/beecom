package bee.basket.iam.service;

import bee.basket.identityserver.dao.RoleDao;
import bee.basket.identityserver.dao.UserDao;
import bee.basket.identityserver.dao.UserIdentityDao;
import bee.basket.identityserver.entity.RoleEntity;
import bee.basket.identityserver.entity.UserEntity;
import bee.basket.identityserver.entity.UserIdentity;
import bee.basket.commonutil.constant.IdentityProvider;
import bee.basket.commonutil.constant.RoleEnum;
import bee.basket.commonutil.contract.service.UserService;
import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.Permission;
import bee.basket.commonutil.model.Role;
import bee.basket.commonutil.model.User;
import bee.basket.commonutil.model.request.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final UserIdentityDao userIdentityDao;
    private final PasswordEncoder passwordEncoder;
    private final RoleDao roleDao;

    UserServiceImpl(
            UserDao userDao,
            UserIdentityDao userIdentityDao,
            PasswordEncoder passwordEncoder,
            RoleDao roleDao
    ) {
        this.userDao = userDao;
        this.userIdentityDao = userIdentityDao;
        this.passwordEncoder = passwordEncoder;
        this.roleDao = roleDao;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) throws UsernameAlreadyExistException {
        if (userDao.existsByEmail(request.email())) {
            throw new UsernameAlreadyExistException("Email already exists");
        }
        if (checkIfExist(request.username())) {
            throw new UsernameAlreadyExistException("Username already exists");
        }

        RoleEntity userRole = roleDao.findByRole(RoleEnum.USER.value());
        if (userRole == null) {
            throw new IllegalStateException("The default user role is not configured");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(request.firstName());
        userEntity.setLastName(request.lastName());
        userEntity.setEmail(request.email());
        userEntity.setEnabled(true);
        userEntity.setRole(Set.of(userRole));

        UserIdentity identity = new UserIdentity();
        identity.setProvider(IdentityProvider.LOCAL);
        identity.setUsername(request.username());
        identity.setPasswordHash(passwordEncoder.encode(request.password()));
        identity.setEnabled(true);
        userEntity.addIdentity(identity);

        UserEntity savedUser = userDao.save(userEntity);
        Set<Role> roles = savedUser.getRole().stream()
                .map(role -> new Role(
                        role.getRole(),
                        role.getPermissions().stream()
                                .map(permission -> new Permission(permission.getPermission()))
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toSet());
        return new User(
                savedUser.getId().toString(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.isEnabled(),
                roles
        );
    }

    @Override
    public boolean checkIfExist(String username) {
        return userIdentityDao.existsByProviderAndUsername(IdentityProvider.LOCAL, username);
    }
}
