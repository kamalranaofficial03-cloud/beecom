package bee.basket.commonutil.model;

import java.util.Set;

public record User(
        String id,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        Set<Role> roles
) {
}