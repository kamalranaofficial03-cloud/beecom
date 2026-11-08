package bee.basket.commonutil.service;

import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.request.UserRM;
import bee.basket.commonutil.model.response.User;

public interface UserService {
    User<String> create(UserRM user) throws UsernameAlreadyExistException;

    boolean checkIfExist(String username);
}
