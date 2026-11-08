package bee.basket.authserver.service;

import bee.basket.authserver.dao.RoleDao;
import bee.basket.authserver.dao.UserDao;
import bee.basket.authserver.entity.RoleEntity;
import bee.basket.authserver.entity.UserEntity;
import bee.basket.commonutil.constant.RoleEnum;
import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.request.UserRM;
import bee.basket.commonutil.model.response.User;
import bee.basket.commonutil.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final RoleDao roleDao;

    UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder, RoleDao roleDao){
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.roleDao = roleDao;
    }

    @Override
    public User create(UserRM user) throws UsernameAlreadyExistException {
        if(checkIfExist(user.username())){
            throw new UsernameNotFoundException("user already exist");
        }
        UserEntity entity = new UserEntity();
        entity.setAccountLocked(false);
        entity.setEnabled(true);
        entity.setUsername(user.username());
        entity.setPassword(passwordEncoder.encode(user.password()));
        RoleEntity userRole = this.roleDao.findByRole(RoleEnum.USER.value());
        entity.setRole(Set.of(userRole));
        entity = this.userDao.save(entity);
        return new User(entity.getUsername(),null, user.enabled(),new HashSet<>(),"user created successfully.");
    }

    @Override
    public boolean checkIfExist(String username){
        return this.userDao.checkIfExist(username);
    }
}
