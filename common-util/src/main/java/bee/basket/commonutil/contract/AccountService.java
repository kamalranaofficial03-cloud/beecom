package bee.basket.commonutil.contract;

import bee.basket.commonutil.model.Account;

public interface AccountService {
    Account findAccount(String username);

    Account addAccount(Account account);
}