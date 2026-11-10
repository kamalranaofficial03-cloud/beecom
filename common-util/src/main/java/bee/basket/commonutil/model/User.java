package bee.basket.commonutil.model;

import java.util.Set;

public record User(String username, String password, boolean enabled, Set<Role> roles)  {
}