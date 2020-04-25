package com.rfc.transactions.Exceptions;

public class ZeroBalanceException extends RuntimeException {

    public ZeroBalanceException(String message) {
        super(message);
    }
}
