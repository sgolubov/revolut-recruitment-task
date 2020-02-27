package ua.com.golubov.revolut.service;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.com.golubov.revolut.db.AccountRepository;
import ua.com.golubov.revolut.db.TransactionsRepository;
import ua.com.golubov.revolut.domain.Account;
import ua.com.golubov.revolut.dto.req.MoneyTransferReqDto;
import ua.com.golubov.revolut.dto.req.TopUpReqDto;
import ua.com.golubov.revolut.exception.AccountNotFoundException;
import ua.com.golubov.revolut.exception.NotEnoughFundsForTransferException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(JUnit4.class)
public class TransactionsServiceTest {

    @Rule
    public JdbiRule jdbiRule = JdbiRule.h2()
            .withMigration(Migration.before().withDefaultPath());

    private TransactionsService underTest;
    private AccountRepository accountRepository;

    @Before
    public void setUp() {
        Jdbi jdbi = jdbiRule.getJdbi();
        accountRepository = new AccountRepository(jdbi);
        underTest = new TransactionsService(
                accountRepository,
                new TransactionsRepository(jdbi),
                jdbi
        );
    }

    @Test
    public void shouldExecuteTheTransfer() {
        // Given
        Long idFrom = 998L;
        Long idTo = 999L;
        BigDecimal transactionAmount = new BigDecimal("1000");

        MoneyTransferReqDto moneyTransferReqDto = new MoneyTransferReqDto();
        moneyTransferReqDto.setAmount(transactionAmount);
        moneyTransferReqDto.setFromAcc(idFrom);
        moneyTransferReqDto.setToAcc(idTo);

        // When
        Account fromBeforeTransaction = accountRepository.getAccount(idFrom).get();
        Account toBeforeTransaction = accountRepository.getAccount(idTo).get();

        underTest.executeMoneyTransfer(moneyTransferReqDto);

        Account fromAfterTransaction = accountRepository.getAccount(idFrom).get();
        Account toAfterTransaction = accountRepository.getAccount(idTo).get();

        // Then
        assertThat(fromBeforeTransaction)
                .usingRecursiveComparison()
                .ignoringFields("balance", "latestActivity")
                .isEqualTo(fromAfterTransaction);
        assertThat(fromBeforeTransaction.getBalance())
                .isEqualTo(fromAfterTransaction.getBalance().add(transactionAmount));

        assertThat(toBeforeTransaction)
                .usingRecursiveComparison()
                .ignoringFields("balance", "latestActivity")
                .isEqualTo(toAfterTransaction);
        assertThat(toBeforeTransaction.getBalance())
                .isEqualTo(toAfterTransaction.getBalance().subtract(transactionAmount));
    }

    @Test
    public void shouldFailWhenAccountDoesntExists() {
        // Given
        MoneyTransferReqDto moneyTransferReqDto = new MoneyTransferReqDto();
        moneyTransferReqDto.setAmount(new BigDecimal("1000"));
        moneyTransferReqDto.setFromAcc(999L);
        moneyTransferReqDto.setToAcc(1000L);

        // When-Then
        assertThatThrownBy(() -> underTest.executeMoneyTransfer(moneyTransferReqDto))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    public void shouldFailWhenNotEnoughMoneyForTransfer() {
        // Given
        MoneyTransferReqDto moneyTransferReqDto = new MoneyTransferReqDto();
        moneyTransferReqDto.setAmount(new BigDecimal("5000"));
        moneyTransferReqDto.setFromAcc(998L);
        moneyTransferReqDto.setToAcc(999L);

        // When-Then
        assertThatThrownBy(() -> underTest.executeMoneyTransfer(moneyTransferReqDto))
                .isInstanceOf(NotEnoughFundsForTransferException.class);
    }

    @Test
    public void shouldTopUpAccount() {
        // Given
        Long idTo = 999L;
        BigDecimal transactionAmount = new BigDecimal("1000");

        TopUpReqDto topUpReqDto = new TopUpReqDto();
        topUpReqDto.setAmount(transactionAmount);
        topUpReqDto.setToAcc(idTo);

        // When
        Account toBeforeTransaction = accountRepository.getAccount(idTo).get();

        underTest.topUpAccount(topUpReqDto);

        Account toAfterTransaction = accountRepository.getAccount(idTo).get();

        // Then
        assertThat(toBeforeTransaction)
                .usingRecursiveComparison()
                .ignoringFields("balance", "latestActivity")
                .isEqualTo(toAfterTransaction);
        assertThat(toBeforeTransaction.getBalance())
                .isEqualTo(toAfterTransaction.getBalance().subtract(transactionAmount));
    }

    @Test
    public void shouldFailToTopUpNotExistingAccount() {
        // Given
        TopUpReqDto topUpReqDto = new TopUpReqDto();
        topUpReqDto.setAmount(new BigDecimal("1000"));
        topUpReqDto.setToAcc(1000L);

        // When-Then
        assertThatThrownBy(() -> underTest.topUpAccount(topUpReqDto))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    public void shouldListTransactionsForAccount() {
        // Given-When-Then
        assertThat(underTest.getTransactions(999L))
                .hasSize(3);
    }

}
