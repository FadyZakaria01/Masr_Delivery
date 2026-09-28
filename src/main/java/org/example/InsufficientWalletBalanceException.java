package org.example;

public class InsufficientWalletBalanceException extends MasrDeliveryException {
    public InsufficientWalletBalanceException(double required, double available) {
        super("Insufficient funds! Required:" + required + "Available:  " + available);
    }
}
