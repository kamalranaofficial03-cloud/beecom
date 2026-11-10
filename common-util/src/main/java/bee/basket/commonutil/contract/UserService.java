package bee.basket.commonutil.contract;

import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.User;

public interface UserService {
    User create(User user) throws UsernameAlreadyExistException;

    boolean checkIfExist(String username);
}
