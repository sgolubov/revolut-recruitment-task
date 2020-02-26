package ua.com.golubov.revolut;

import com.google.inject.Guice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.golubov.revolut.api.AccountApi;
import ua.com.golubov.revolut.api.TransactionApi;
import ua.com.golubov.revolut.config.GuiceModule;
import ua.com.golubov.revolut.db.FlywayMigrator;
import ua.com.golubov.revolut.exception.AccountNotFoundException;
import ua.com.golubov.revolut.exception.BadRequestException;
import ua.com.golubov.revolut.exception.BrokenBusinessFlowException;
import ua.com.golubov.revolut.exception.NotEnoughFundsForTransferException;

import javax.inject.Inject;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final FlywayMigrator flywayMigrator;
    private final AccountApi accountApi;
    private final TransactionApi transactionApi;

    @Inject
    public Application(FlywayMigrator flywayMigrator, AccountApi accountApi, TransactionApi transactionApi) {
        this.flywayMigrator = flywayMigrator;
        this.accountApi = accountApi;
        this.transactionApi = transactionApi;
    }

    public static void main(final String... args) {
        Guice.createInjector(new GuiceModule())
                .getInstance(Application.class)
                .run();
    }

    void run() {
        // Spin up DB
        flywayMigrator.migrate();

        port(8080);

        before("/*", (req, res) -> LOG.info("{}: {}.", req.requestMethod(), req.uri()));
        after("/*", (req, res) ->
                LOG.info("Response with status {} returned for request with URI - {}.", res.status(), req.uri()));

        path("/v1", () -> {
            path("/account", () -> {
                post("", accountApi.createAccountRoute());
                put("/:id", accountApi.updateAccountRoute());
                get("/:id/balance", accountApi.checkBalanceRoute());
                get("/list", accountApi.listAccountsRoute());
            });

            path("/transaction", () -> {
                post("/transfer", transactionApi.createMoneyTransferRoute());
                post("/top-up", transactionApi.createTopUpRoute());
                get("/list/:id", transactionApi.listTransactions());
            });
        });

        // Exception handling
        exception(BrokenBusinessFlowException.class, (exception, request, response) -> {
            response.status(500);
            response.body(exception.getMessage());
        });

        exception(BadRequestException.class, (exception, request, response) -> {
            response.status(400);
            response.body(exception.getMessage());
        });

        exception(AccountNotFoundException.class, (exception, request, response) -> {
            response.status(404);
            response.body(exception.getMessage());
        });

        exception(NotEnoughFundsForTransferException.class, (exception, request, response) -> {
            response.status(400);
            response.body(exception.getMessage());
        });

    }

}
