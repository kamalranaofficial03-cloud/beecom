package bee.basket.commonutil.model;

import java.util.Set;

public record Role(String role, Set<Permission> permissions) {
}
