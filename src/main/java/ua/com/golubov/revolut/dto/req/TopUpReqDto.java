package ua.com.golubov.revolut.dto.req;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TopUpReqDto {

    @NotNull
    @Min(1)
    private Long toAcc;
    @NotNull
    @Min(1)
    private BigDecimal amount;

}
