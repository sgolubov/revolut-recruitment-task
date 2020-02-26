package ua.com.golubov.revolut.db;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.com.golubov.revolut.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class AccountRepositoryTest {

    @Rule
    public JdbiRule jdbiRule = JdbiRule.h2()
            .withMigration(Migration.before().withDefaultPath());

    private AccountRepository underTest;
    private Jdbi jdbi;

    @Before
    public void setup() {
        jdbi = jdbiRule.getJdbi();
        underTest = new AccountRepository(jdbi);
    }

    /**
     * Here we check that account record is locked and update can't be done
     * until transaction is committed and lock is released
     */
    @Test
    public void shouldLockRecordForWrite() {
        // Given
        Long id = 999L;
        long pause = 1_000L;
        Account account = new Account(id, "Johnny Doe", new BigDecimal("1000.00"), LocalDateTime.now());

        // When
        new Thread(() -> jdbi.inTransaction(handle -> {
            Optional<Account> accountOptional = underTest.getAndLockAccount(handle, id);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                // swallow exception
            }
            underTest.update(account);
            return accountOptional;
        })).start();

        account.setName("John Doe Jr.");
        account.setBalance(new BigDecimal("2000.00"));
        account.setLatestActivity(LocalDateTime.now());

        long before = System.currentTimeMillis();
        underTest.update(account);
        long after = System.currentTimeMillis();

        Account updated = underTest.getAccount(id)
                .get();

        // Then
        assertThat(after - before)
                .isGreaterThan(pause);
        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringFields("createdDate")
                .isEqualTo(account);
    }

    @Test
    public void shouldReturnListOfAccounts() {
        // Given-When
        List<Account> accounts = underTest.getAccounts();

        // Then
        assertThat(accounts)
                .hasSize(2);
    }

    @Test
    public void shouldUpdateExistingAccount() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        BigDecimal balance = new BigDecimal("1000.00");
        Long id = 999L;
        Account account = new Account(id, "Johnny Doe", balance, now);

        // When
        account = underTest.update(account);
        Account updated = underTest.getAccount(id)
                .get();

        // Then
        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringFields("createdDate")
                .isEqualTo(account);
    }

    @Test
    public void shouldCreateNewAccount() {
        // Given
        String name = "Brad Pitt";
        Account account = new Account();
        account.setName(name);

        // When
        Long id = underTest.create(account);
        Account created = underTest.getAccount(id)
                .get();

        // Then
        assertThat(created)
                .hasFieldOrPropertyWithValue("name", name)
                .hasFieldOrPropertyWithValue("balance", BigDecimal.ZERO);
    }

}
