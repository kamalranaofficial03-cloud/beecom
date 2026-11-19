package bee.basket.commonutil.model.request;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String username,
        String password
) {
}
