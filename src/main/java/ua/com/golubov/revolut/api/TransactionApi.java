package ua.com.golubov.revolut.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import spark.Route;
import ua.com.golubov.revolut.dto.req.MoneyTransferReqDto;
import ua.com.golubov.revolut.dto.req.TopUpReqDto;
import ua.com.golubov.revolut.service.TransactionsService;

import javax.validation.Validator;

@Singleton
public class TransactionApi extends BaseApi {

    private final TransactionsService transactionsService;

    @Inject
    public TransactionApi(ObjectMapper objectMapper, TransactionsService transactionsService, Validator validator) {
        super(objectMapper, validator);
        this.transactionsService = transactionsService;
    }

    public Route createMoneyTransferRoute() {
        return (req, res) -> objectMapper.writeValueAsString(
                transactionsService.executeMoneyTransfer(convertAndValidate(req.body(), MoneyTransferReqDto.class)
                ));
    }

    public Route createTopUpRoute() {
        return (req, res) -> objectMapper.writeValueAsString(
                transactionsService.topUpAccount(convertAndValidate(req.body(), TopUpReqDto.class)
                ));
    }

    public Route listTransactions() {
        return (req, res) -> objectMapper.writeValueAsString(
                transactionsService.getTransactions(getId(req.params(":id")))
        );
    }

}
