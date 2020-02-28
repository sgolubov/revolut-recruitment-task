package ua.com.golubov.revolut.db;

import org.assertj.core.data.Offset;
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
import java.util.concurrent.CountDownLatch;

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
     * Here we check that account record is locked and another update can't be done
     * until transaction is committed and lock is released
     */
    @Test
    public void shouldLockRecordForWrite() throws InterruptedException {

        // Can be executed multiple times to ensure correctness
        for (int i = 0; i < 1; i++) {
            // Given

            // Use latch to make sure record is locked before trying to execute second update
            CountDownLatch latch = new CountDownLatch(1);

            Long id = 999L;
            long pause = 1_500L;
            Account account = new Account(id, "Johnny Doe", new BigDecimal("1000.00"), LocalDateTime.now());

            // When
            new Thread(() -> jdbi.inTransaction(handle -> {
                Optional<Account> accountOptional = underTest.getAndLockAccount(handle, id);
                // record locked
                latch.countDown();
                try {
                    // keep lock for a moment
                    Thread.sleep(pause);
                } catch (InterruptedException e) {
                    // swallow exception
                }
                underTest.update(account);
                return accountOptional;
            })).start();

            // now let's try to execute second update while record is locked
            latch.await();

            account.setName("John Doe Jr.");
            account.setBalance(new BigDecimal("2000.00"));
            account.setLatestActivity(LocalDateTime.now());

            long before = System.currentTimeMillis();
            underTest.update(account);
            long after = System.currentTimeMillis();

            Account updated = underTest.getAccount(id)
                    .get();

            // Then

            // check that second update wasn't executed before the first
            assertThat(after - before)
                    .isCloseTo(pause, Offset.offset(200L));

            // check that actual state of the data in db corresponds to second update
            assertThat(updated)
                    .usingRecursiveComparison()
                    .ignoringFieldsOfTypes(LocalDateTime.class)
                    .isEqualTo(account);

            assertThat(updated.getLatestActivity())
                    .isEqualToIgnoringNanos(account.getLatestActivity());
        }
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
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(account);

        assertThat(updated.getLatestActivity())
                .isEqualToIgnoringNanos(account.getLatestActivity());
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
