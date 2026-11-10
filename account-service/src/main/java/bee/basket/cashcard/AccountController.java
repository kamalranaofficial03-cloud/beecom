package bee.basket.cashcard;

import bee.basket.commonutil.model.Account;
import bee.basket.commonutil.model.request.AccountRM;
import bee.basket.commonutil.model.response.AccountRes;
import bee.basket.commonutil.contract.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountServiceImp accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{username}")
    @PostAuthorize("returnObject.body.owner == authentication.name")
    public ResponseEntity<AccountRes<?>> findById(@PathVariable String username) {
        Account account = this.accountService.findAccount(username);
        return ResponseEntity.ok(new AccountRes(account, "201 created,Successful."));
    }

    @PostMapping
    public ResponseEntity<AccountRes<?>> createAccount(
            @RequestBody AccountRM requestBody,
            UriComponentsBuilder uriComponentsBuilder,
            Authentication authentication) {
        if(authentication.getName() != requestBody.body().username()){
            throw new RuntimeException("invalid user");
        }
        var responseBody = this.accountService.addAccount(
                requestBody.body());

        var location = uriComponentsBuilder
                .path("/account/{username}")
                .buildAndExpand(responseBody.username())
                .toUri();

        return ResponseEntity.created(location).body(new AccountRes<>(responseBody, "201 create,successfull."));
    }

}
