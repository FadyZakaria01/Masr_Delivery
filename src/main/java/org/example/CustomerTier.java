package org.example;

public enum CustomerTier {
    BRONZE(0.0),
    SILVER(0.10),
    GOLD(1.00);


    private final double deliveryDiscountRate;

    CustomerTier(double deliveryDiscountRate) {
        this.deliveryDiscountRate = deliveryDiscountRate;
    }

    public double getDeliveryDiscountRate() {
        return deliveryDiscountRate;
    }

}
