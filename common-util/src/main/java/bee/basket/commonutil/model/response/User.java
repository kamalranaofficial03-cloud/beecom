package bee.basket.commonutil.model.response;

import bee.basket.commonutil.model.ResponseBody;
import bee.basket.commonutil.model.Role;

import java.util.Set;

public record User<T>(String username, String password, String enabled, Set<Role> roles, T message ) implements ResponseBody<T> {
}