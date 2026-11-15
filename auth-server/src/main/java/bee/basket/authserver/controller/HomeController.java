package bee.basket.authserver.controller;

import bee.basket.authserver.service.AccountClientService;
import bee.basket.commonutil.model.response.AccountRes;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("home")
public class HomeController {
	private final AccountClientService accountService;

    public HomeController(AccountClientService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/")
	public String home() {
		return "redirect:/account";
	}

	@GetMapping("/account")
	public List<AccountRes<?>> account(Authentication authentication) {
		return accountService.accountDetails(authentication.getName());
	}

}
