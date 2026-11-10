package bee.basket.cashcard;

import java.util.List;
import java.util.stream.Collectors;
import bee.basket.cashcard.entity.AccountEntity;
import bee.basket.cashcard.entity.AddressEntity;
import bee.basket.commonutil.model.Account;
import bee.basket.commonutil.contract.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImp implements AccountService {

	private final AccountRepository accountRepository;

	public AccountServiceImp(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Account findAccount(String username) {
		AccountEntity account = this.accountRepository.findByUsername(username);
		if(account == null){
			throw new RuntimeException("account not found");
		}
		return account.model();
	}

	@Override
	public Account addAccount(Account account) {
		AccountEntity result = this.accountRepository.findByUsername(account.username());
		if(result != null){
			throw new RuntimeException("account already exist");
		}
		result = AccountEntity.create(account);
		List<AddressEntity> addresses=account.address().stream().map(AddressEntity::create).collect(Collectors.toUnmodifiableList());
		result.setAddresses(addresses);
		result= this.accountRepository.save(result);
		return result.model();
	}

}
