package ua.com.golubov.revolut.dto.resp;

import lombok.Value;
import ua.com.golubov.revolut.domain.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class AccountTransactionRespDto {

    private final Long transactionId;
    private final Long fromAcc;
    private final Long toAcc;
    private final BigDecimal amount;
    private final Type type;
    private final LocalDateTime executionDate;

}
