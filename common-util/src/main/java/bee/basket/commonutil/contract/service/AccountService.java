package bee.basket.commonutil.contract.service;

import bee.basket.commonutil.model.Account;

public interface AccountService {
    Account findAccount(String username);

    Account addAccount(Account account);
}