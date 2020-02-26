package ua.com.golubov.revolut.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import ua.com.golubov.revolut.domain.Account;

import java.util.List;
import java.util.Optional;

@Singleton
public class AccountRepository {

    private static final String ACCOUNT_LOCK_QUERY = "select * from ACCOUNT where id = :id for update";
    private static final String GET_ACCOUNT_QUERY = "select * from ACCOUNT where id = :id";
    private static final String ACCOUNT_SEQ_QUERY = "select ACCOUNT_SEQ.nextval from dual";
    private static final String CREATE_ACCOUNT_QUERY =
            "insert into ACCOUNT(id, name) values (:id, :name)";
    private static final String UPDATE_ACCOUNT_QUERY =
            "update ACCOUNT set name = :name, balance = :balance, latest_activity = :latestActivity where id = :id";
    private static final String LIST_ACCOUNTS_QUERY = "select * from ACCOUNT order by latest_activity";

    private final Jdbi jdbi;

    @Inject
    public AccountRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Long create(Account account) {
        return jdbi.withHandle(handle -> handle.inTransaction(h -> {
            Long id = h.createQuery(ACCOUNT_SEQ_QUERY)
                    .mapTo(Long.class)
                    .one();

            account.setId(id);

            h.createUpdate(CREATE_ACCOUNT_QUERY)
                    .bindBean(account)
                    .execute();

            return id;
        }));
    }

    public Optional<Account> getAndLockAccount(Handle handle, Long id) {
        return handle.createQuery(ACCOUNT_LOCK_QUERY)
                .bind("id", id)
                .mapToBean(Account.class)
                .findOne();
    }

    public Optional<Account> getAccount(Long id) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_ACCOUNT_QUERY)
                .bind("id", id)
                .mapToBean(Account.class)
                .findOne());
    }

    public Account update(Account account) {
        return jdbi.withHandle(handle -> handle.inTransaction(h -> {
                    h.createUpdate(UPDATE_ACCOUNT_QUERY)
                            .bindBean(account)
                            .execute();
                    return account;
                })
        );
    }

    public List<Account> getAccounts() {
        return jdbi.withHandle(handle -> handle.createQuery(LIST_ACCOUNTS_QUERY)
                .mapToBean(Account.class)
                .list());
    }

}
