package ua.com.golubov.revolut.db;

import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.com.golubov.revolut.domain.AccountTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.golubov.revolut.domain.Type.TRANSFER;

@RunWith(JUnit4.class)
public class TransactionsRepositoryTest {

    @Rule
    public JdbiRule jdbiRule = JdbiRule.h2()
            .withMigration(Migration.before().withDefaultPath());

    private TransactionsRepository underTest;

    @Before
    public void setup() {
        underTest = new TransactionsRepository(jdbiRule.getJdbi());
    }

    @Test
    public void shouldListTransactionsForAccount() {
        // Given-When
        List<AccountTransaction> accountTransactions = underTest.listTransactions(998L);

        // Then
        assertThat(accountTransactions)
                .hasSize(3);
    }

    @Test
    public void shouldAddNewTransactionsForAccount() {
        // Given
        AccountTransaction accountTransaction = new AccountTransaction(998L, 999L,
                new BigDecimal("10000"), TRANSFER, LocalDateTime.now());

        // When
        underTest.addNewTransaction(accountTransaction);

        // Then
        assertThat(underTest.listTransactions(998L))
                .hasSize(4);
    }

}
