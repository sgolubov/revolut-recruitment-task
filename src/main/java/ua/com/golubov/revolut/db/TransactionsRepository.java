package ua.com.golubov.revolut.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import ua.com.golubov.revolut.domain.AccountTransaction;

import java.util.List;

@Singleton
public class TransactionsRepository {

    private static final String TRANSACTION_SEQ_QUERY = "select ACCOUNT_TRANSACTION_SEQ.nextval from dual";
    private static final String LIST_TRANSACTIONS_FOR_ACCOUNT =
            "select * from ACCOUNT_TRANSACTION where from_acc = :id or to_acc = :id";
    private static final String INSERT_TRANSACTION_QUERY =
            "insert into ACCOUNT_TRANSACTION(id, from_acc, to_acc, amount, type) " +
                    "values (:id, :fromAcc, :toAcc, :amount, :type)";

    private final Jdbi jdbi;

    @Inject
    public TransactionsRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Long addNewTransaction(AccountTransaction accountTransaction) {
        return jdbi.withHandle(handle -> {

            Long id = handle.createQuery(TRANSACTION_SEQ_QUERY)
                    .mapTo(Long.class)
                    .one();

            accountTransaction.setId(id);

            handle.createUpdate(INSERT_TRANSACTION_QUERY)
                    .bindBean(accountTransaction)
                    .execute();

            return id;
        });
    }

    public List<AccountTransaction> listTransactions(Long accountId) {
        return jdbi.withHandle(handle -> handle.createQuery(LIST_TRANSACTIONS_FOR_ACCOUNT)
                .bind("id", accountId)
                .mapToBean(AccountTransaction.class)
                .list());
    }

}
