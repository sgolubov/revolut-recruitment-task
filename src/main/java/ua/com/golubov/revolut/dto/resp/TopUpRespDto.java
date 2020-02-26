package ua.com.golubov.revolut.dto.resp;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class TopUpRespDto {

    private final Long transactionId;
    private final BigDecimal currentBalance;

}
