package ua.com.golubov.revolut.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import ua.com.golubov.revolut.db.AccountRepository;
import ua.com.golubov.revolut.db.TransactionsRepository;
import ua.com.golubov.revolut.domain.Account;
import ua.com.golubov.revolut.domain.AccountTransaction;
import ua.com.golubov.revolut.dto.req.MoneyTransferReqDto;
import ua.com.golubov.revolut.dto.req.TopUpReqDto;
import ua.com.golubov.revolut.dto.resp.AccountTransactionRespDto;
import ua.com.golubov.revolut.dto.resp.MoneyTransferRespDto;
import ua.com.golubov.revolut.dto.resp.TopUpRespDto;
import ua.com.golubov.revolut.exception.AccountNotFoundException;
import ua.com.golubov.revolut.exception.NotEnoughFundsForTransferException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ua.com.golubov.revolut.domain.Type.TOP_UP;
import static ua.com.golubov.revolut.domain.Type.TRANSFER;

@Singleton
public class TransactionsService {

    private final AccountRepository accountRepository;
    private final TransactionsRepository transactionsRepository;
    private final Jdbi jdbi;

    @Inject
    public TransactionsService(
            AccountRepository accountRepository,
            TransactionsRepository transactionsRepository,
            Jdbi jdbi) {
        this.accountRepository = accountRepository;
        this.transactionsRepository = transactionsRepository;
        this.jdbi = jdbi;
    }

    public MoneyTransferRespDto executeMoneyTransfer(MoneyTransferReqDto moneyTransferReqDto) {
        return jdbi.withHandle(handle -> handle.inTransaction(h -> {

            Account from = accountRepository.getAndLockAccount(h, moneyTransferReqDto.getFromAcc())
                    .orElseThrow(() -> new AccountNotFoundException(moneyTransferReqDto.getFromAcc()));
            Account to = accountRepository.getAndLockAccount(h, moneyTransferReqDto.getToAcc())
                    .orElseThrow(() -> new AccountNotFoundException(moneyTransferReqDto.getToAcc()));

            BigDecimal amount = moneyTransferReqDto.getAmount();

            if (amount.compareTo(from.getBalance()) > 0)
                throw new NotEnoughFundsForTransferException();

            LocalDateTime now = LocalDateTime.now();

            from.setBalance(from.getBalance().subtract(amount));
            from.setLatestActivity(now);
            accountRepository.update(from);

            to.setBalance(to.getBalance().add(amount));
            to.setLatestActivity(now);
            accountRepository.update(to);

            return new MoneyTransferRespDto(
                    transactionsRepository.addNewTransaction(mapToTransaction(moneyTransferReqDto)));
        }));
    }

    public TopUpRespDto topUpAccount(TopUpReqDto topUpReqDto) {
        return jdbi.withHandle(handle -> handle.inTransaction(h -> {
            Account to = accountRepository.getAndLockAccount(h, topUpReqDto.getToAcc())
                    .orElseThrow(() -> new AccountNotFoundException(topUpReqDto.getToAcc()));

            BigDecimal balanceAfter = to.getBalance().add(topUpReqDto.getAmount());

            to.setBalance(balanceAfter);
            to.setLatestActivity(LocalDateTime.now());
            accountRepository.update(to);

            Long transactionId = transactionsRepository.addNewTransaction(mapToTransaction(topUpReqDto));

            return new TopUpRespDto(transactionId, balanceAfter);
        }));
    }

    public List<AccountTransactionRespDto> getTransactions(Long accountId) {
        return transactionsRepository.listTransactions(accountId)
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private AccountTransaction mapToTransaction(MoneyTransferReqDto moneyTransferReqDto) {
        return new AccountTransaction(
                moneyTransferReqDto.getFromAcc(),
                moneyTransferReqDto.getToAcc(),
                moneyTransferReqDto.getAmount(),
                TRANSFER,
                LocalDateTime.now()
        );
    }

    private AccountTransaction mapToTransaction(TopUpReqDto topUpReqDto) {
        return new AccountTransaction(
                topUpReqDto.getToAcc(),
                topUpReqDto.getToAcc(),
                topUpReqDto.getAmount(),
                TOP_UP,
                LocalDateTime.now()
        );
    }

    private AccountTransactionRespDto mapToTransactionResponse(AccountTransaction accountTransaction) {
        return new AccountTransactionRespDto(
                accountTransaction.getId(),
                accountTransaction.getFromAcc(),
                accountTransaction.getToAcc(),
                accountTransaction.getAmount(),
                accountTransaction.getType(),
                accountTransaction.getExecutionDate()
        );
    }

}
