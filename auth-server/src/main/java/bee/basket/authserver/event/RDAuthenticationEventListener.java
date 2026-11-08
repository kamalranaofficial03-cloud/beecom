package bee.basket.authserver.event;

import bee.basket.authserver.entity.UserEntity;
import bee.basket.authserver.dao.UserDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.datasource", name="provider", havingValue = "rd", matchIfMissing =true)
public class RDAuthenticationEventListener implements UserDetailsService {
    private final UserDao userDao;

    RDAuthenticationEventListener(UserDao userDao){
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = this.userDao.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("invalid username/password");
        }
        String[] role = user.getRole().stream().map(val->val.getRole()).toArray(String[]::new);
        String[] permission = user.getRole().stream().flatMap(val-> val.getPermissions().stream()).map(val->val.getPermission()).toArray(String[]::new);
        UserDetails userDetails = User.builder()
                .username(username)
                .password(user.getPassword())
                .accountExpired(false)
                .accountLocked(user.isAccountLocked())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .roles(role)
                .authorities(permission)
                .build();
        return userDetails;
    }
}
