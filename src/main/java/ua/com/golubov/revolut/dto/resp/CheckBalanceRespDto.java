package ua.com.golubov.revolut.dto.resp;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
public class CheckBalanceRespDto {

    private final Long id;
    private final BigDecimal balance;
    private final LocalDateTime latestActivity;

}
