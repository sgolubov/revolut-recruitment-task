package ua.com.golubov.revolut.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Account {

    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime latestActivity;
    private LocalDateTime createdDate;

    public Account(String name, LocalDateTime latestActivity) {
        this.name = name;
        this.latestActivity = latestActivity;
    }

}
