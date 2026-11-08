package bee.basket.commonutil.model.request;

import bee.basket.commonutil.model.Role;

import java.util.Set;

public record UserRM(String username, String password, String enabled, Set<Role> roles) {
}
