package ua.com.golubov.revolut.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ua.com.golubov.revolut.db.AccountRepository;
import ua.com.golubov.revolut.domain.Account;
import ua.com.golubov.revolut.dto.req.AccountReqDto;
import ua.com.golubov.revolut.dto.resp.CheckBalanceRespDto;
import ua.com.golubov.revolut.dto.resp.CreateAccountRespDto;
import ua.com.golubov.revolut.exception.AccountNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepositoryMock;
    @InjectMocks
    private AccountService underTest;

    @Test
    public void shouldCheckBalance() {
        // Given
        Long id = 1L;
        doReturn(Optional.of(getAccount()))
                .when(accountRepositoryMock)
                .getAccount(id);

        // When
        CheckBalanceRespDto response = underTest.checkBalance(id);

        // Then
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("balance", new BigDecimal("1000"));

    }

    @Test
    public void shouldThrowAnExceptionForNonExistentAccount() {
        // Given
        Long id = 1L;
        doReturn(Optional.empty())
                .when(accountRepositoryMock)
                .getAccount(id);

        // When-Then
        assertThatThrownBy(() -> underTest.checkBalance(id))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    public void shouldCreateAnAccount() {
        // Given
        AccountReqDto accountReqDto = new AccountReqDto();
        accountReqDto.setName("John Doe");

        doReturn(1L)
                .when(accountRepositoryMock)
                .create(any(Account.class));

        // When
        CreateAccountRespDto response = underTest.createAccount(accountReqDto);

        // Then
        assertThat(response)
                .hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void shouldUpdateAnAccount() {
        // Given
        Long id = 1L;

        AccountReqDto accountReqDto = new AccountReqDto();
        accountReqDto.setName("Johnny Doe");

        Account account = getAccount();

        doReturn(Optional.of(account))
                .when(accountRepositoryMock)
                .getAccount(id);

        doAnswer(i -> i.getArgument(0))
                .when(accountRepositoryMock)
                .update(account);

        // When
        underTest.updateAccount(id, accountReqDto);

        // Then
        verify(accountRepositoryMock).update(account);
    }

    @Test
    public void shouldThrowAnExceptionIfAccountNotFound() {
        // Given
        Long id = 1L;

        AccountReqDto accountReqDto = new AccountReqDto();
        accountReqDto.setName("Johnny Doe");

        doReturn(Optional.empty())
                .when(accountRepositoryMock)
                .getAccount(id);

        // When-Then
        assertThatThrownBy(() -> underTest.updateAccount(id, accountReqDto))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    public void shouldCallRepoForAccounts() {
        underTest.getAccounts();
        verify(accountRepositoryMock).getAccounts();
    }

    private Account getAccount() {
        return new Account(1L, "John Doe", new BigDecimal("1000"), LocalDateTime.now());
    }

}
