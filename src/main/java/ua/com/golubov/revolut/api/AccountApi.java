package ua.com.golubov.revolut.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import spark.Route;
import ua.com.golubov.revolut.dto.req.AccountReqDto;
import ua.com.golubov.revolut.service.AccountService;

import javax.validation.Validator;

@Singleton
public class AccountApi extends BaseApi {

    private final AccountService accountService;

    @Inject
    public AccountApi(ObjectMapper objectMapper, AccountService accountService, Validator validator) {
        super(objectMapper, validator);
        this.accountService = accountService;
    }

    public Route createAccountRoute() {
        return (req, res) -> objectMapper.writeValueAsString(
                accountService.createAccount(convertAndValidate(req.body(), AccountReqDto.class))
        );
    }

    public Route updateAccountRoute() {
        return (req, res) -> {
            accountService.updateAccount(getId(req.params(":id")), convertAndValidate(req.body(), AccountReqDto.class));
            res.status(204);
            return "";
        };
    }

    public Route listAccountsRoute() {
        return (req, res) -> objectMapper.writeValueAsString(
                accountService.getAccounts()
        );
    }

    public Route checkBalanceRoute() {
        return (req, res) -> objectMapper.writeValueAsString(
                accountService.checkBalance(getId(req.params(":id")))
        );
    }

}
