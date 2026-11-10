package bee.basket.authserver.controller;

import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.contract.ResponseBody;
import bee.basket.commonutil.model.request.UserRM;
import bee.basket.commonutil.model.User;
import bee.basket.commonutil.model.response.UserRes;
import bee.basket.commonutil.contract.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    ResponseEntity<ResponseBody<String>> create(@RequestBody UserRM user) throws UsernameAlreadyExistException {
        User responseBody = userService.create(user.body());
        URI location = URI.create("/user/" + responseBody.username());
        return ResponseEntity.created(location).body(new UserRes<>(responseBody, "201 created, successfully."));
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    ResponseEntity<String> usernameAlreadyExist(UsernameAlreadyExistException exception){
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
