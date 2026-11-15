package bee.basket.authserver.service;

import bee.basket.commonutil.model.response.AccountRes;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import java.util.List;

public interface AccountClientService {
    @GetExchange("http://localhost:8080/account/{username}")
    @ClientRegistrationId("oidc-client")
    List<AccountRes<?>> accountDetails(@PathVariable String username);
}