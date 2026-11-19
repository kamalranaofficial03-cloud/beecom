package bee.basket.identityserver.service;

import bee.basket.identityserver.dao.UserIdentityDao;
import bee.basket.identityserver.entity.UserIdentity;
import bee.basket.commonutil.constant.IdentityProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(prefix = "app.datasource", name = "provider", havingValue = "rd")
public class CustomUserDetailsService implements UserDetailsService {
    private final UserIdentityDao userIdentityDao;

    public CustomUserDetailsService(UserIdentityDao userIdentityDao) {
        this.userIdentityDao = userIdentityDao;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserIdentity identity = userIdentityDao
                .findByProviderAndUsername(IdentityProvider.LOCAL, username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if (identity.getUser() == null) {
            throw new UsernameNotFoundException("Invalid username or password");
        }

        Stream<String> roleAuthorities = identity.getUser().getRole().stream()
                .map(role -> "ROLE_" + role.getRole().replaceFirst("^ROLE_", "").toUpperCase());
        Stream<String> permissionAuthorities = identity.getUser().getRole().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getPermission());
        String[] authorities = Stream.concat(roleAuthorities, permissionAuthorities)
                .distinct()
                .toArray(String[]::new);

        return User.withUsername(identity.getUsername())
                .password(identity.getPasswordHash())
                .disabled(!identity.isEnabled() || !identity.getUser().isEnabled())
                .authorities(authorities)
                .build();
    }
}
