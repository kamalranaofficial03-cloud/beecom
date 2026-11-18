package bee.basket.commonutil.contract.service;

import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.User;
import bee.basket.commonutil.model.request.RegisterRequest;

public interface UserService {
    User register(RegisterRequest request) throws UsernameAlreadyExistException;

    boolean checkIfExist(String username);
}
