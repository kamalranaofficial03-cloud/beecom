package bee.basket.identityserver.controller;

import bee.basket.commonutil.contract.ResponseBody;
import bee.basket.commonutil.contract.service.UserService;
import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.User;
import bee.basket.commonutil.model.request.RegisterRequest;
import bee.basket.commonutil.model.response.UserRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    ResponseEntity<ResponseBody<String>> create(@RequestBody RegisterRequest request) throws UsernameAlreadyExistException {
        User responseBody = userService.register(request);
        URI location = URI.create("/users/" + responseBody.id());
        return ResponseEntity.created(location).body(new UserRes<>(responseBody, "201 created, successfully."));
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    ResponseEntity<String> usernameAlreadyExist(UsernameAlreadyExistException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
