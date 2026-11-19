package bee.basket.iam.controller;

import bee.basket.commonutil.contract.service.UserService;
import bee.basket.commonutil.exception.UsernameAlreadyExistException;
import bee.basket.commonutil.model.User;
import bee.basket.commonutil.model.request.RegisterRequest;
import bee.basket.commonutil.model.response.UserRes;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void createReturnsCreatedLocationAndUserPayload() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        RegisterRequest request = new RegisterRequest("Ada", "Lovelace", "ada@example.com", "ada", "secret");
        User user = new User("user-id", "Ada", "Lovelace", "ada@example.com", true, Set.of());
        when(userService.register(request)).thenReturn(user);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/users/user-id");
        assertThat(response.getBody()).isInstanceOf(UserRes.class);
        UserRes<?> body = (UserRes<?>) response.getBody();
        assertThat(body.body()).isEqualTo(user);
        assertThat(body.message()).isEqualTo("201 created, successfully.");
        verify(userService).register(request);
    }

    @Test
    void usernameAlreadyExistsReturnsBadRequestAndExceptionMessage() {
        UserController controller = new UserController(mock(UserService.class));

        var response = controller.usernameAlreadyExist(new UsernameAlreadyExistException("Username already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Username already exists");
    }
}
