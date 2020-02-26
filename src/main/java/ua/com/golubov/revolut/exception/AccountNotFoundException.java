package ua.com.golubov.revolut.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super(String.format("Account with id - %d doesn't exists.", id));
    }
}
