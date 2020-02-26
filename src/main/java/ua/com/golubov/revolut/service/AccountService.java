package ua.com.golubov.revolut.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ua.com.golubov.revolut.db.AccountRepository;
import ua.com.golubov.revolut.domain.Account;
import ua.com.golubov.revolut.dto.req.AccountReqDto;
import ua.com.golubov.revolut.dto.resp.CheckBalanceRespDto;
import ua.com.golubov.revolut.dto.resp.CreateAccountRespDto;
import ua.com.golubov.revolut.exception.AccountNotExistsException;
import ua.com.golubov.revolut.exception.BrokenBusinessFlowException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class AccountService {

    private final AccountRepository accountRepository;

    @Inject
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public CheckBalanceRespDto checkBalance(Long accountId) {
        return accountRepository.getAccount(accountId)
                .map(account ->
                        new CheckBalanceRespDto(account.getId(), account.getBalance(), account.getLatestActivity()))
                .orElseThrow(() -> new AccountNotExistsException(accountId));
    }

    public CreateAccountRespDto createAccount(AccountReqDto accountReqDto) {
        return Optional.of(accountReqDto)
                .map(this::mapToAccount)
                .map(accountRepository::create)
                .map(CreateAccountRespDto::new)
                .orElseThrow(BrokenBusinessFlowException::new);
    }

    public void updateAccount(Long id, AccountReqDto accountReqDto) {
        accountRepository.getAccount(id)
                .map(account -> {
                    account.setName(accountReqDto.getName());
                    account.setLatestActivity(LocalDateTime.now());
                    return account;
                })
                .map(accountRepository::update)
                .orElseThrow(() -> new AccountNotExistsException(id));
    }

    public List<Account> getAccounts() {
        return accountRepository.getAccounts();
    }

    private Account mapToAccount(AccountReqDto accountReqDto) {
        return new Account(
                accountReqDto.getName(),
                LocalDateTime.now()
        );
    }

}
