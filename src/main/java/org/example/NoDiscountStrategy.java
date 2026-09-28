package org.example;

public class NoDiscountStrategy implements PromotionStrategy {

    @Override
    public double calculateDiscount(double subtotal) {
        return 0.0;
    }
}
