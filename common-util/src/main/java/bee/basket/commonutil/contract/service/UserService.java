package bee.basket.commonutil.contract.service;

import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.User;

public interface UserService {
    User create(User user) throws UsernameAlreadyExistException;

    boolean checkIfExist(String username);
}
