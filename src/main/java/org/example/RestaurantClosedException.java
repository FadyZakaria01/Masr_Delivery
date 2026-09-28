package org.example;

public class RestaurantClosedException extends MasrDeliveryException {
    public RestaurantClosedException(String restaurantName) {
        super("the" + restaurantName + "is closed now");
    }
}
