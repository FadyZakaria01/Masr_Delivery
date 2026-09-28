package org.example;

public class RiderBusyException extends MasrDeliveryException {
    public RiderBusyException(String riderName) {
        super("rider with name"+riderName+" is busy");
    }
}
