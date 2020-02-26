package ua.com.golubov.revolut.exception;

public class AccountNotExistsException extends RuntimeException {

    public AccountNotExistsException(Long id) {
        super(String.format("Account with id - %d doesn't exists.", id));
    }
}
