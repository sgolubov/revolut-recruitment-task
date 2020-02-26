package ua.com.golubov.revolut.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AccountTransaction {

    private Long id;
    private Long fromAcc;
    private Long toAcc;
    private BigDecimal amount;
    private Type type;
    private LocalDateTime executionDate;

    public AccountTransaction(Long fromAcc, Long toAcc, BigDecimal amount, Type type, LocalDateTime executionDate) {
        this.fromAcc = fromAcc;
        this.toAcc = toAcc;
        this.amount = amount;
        this.type = type;
        this.executionDate = executionDate;
    }
}
