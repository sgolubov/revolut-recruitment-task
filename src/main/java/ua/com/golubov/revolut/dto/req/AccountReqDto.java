package ua.com.golubov.revolut.dto.req;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class AccountReqDto {

    @NotNull
    @Length(max = 100)
    private String name;

}
