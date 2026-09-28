package org.example;

public class PercentageDiscountStrategy implements PromotionStrategy {

    private final double percentage;
    public PercentageDiscountStrategy(double percentage) {
        this.percentage = percentage;
    }
    @Override
    public double calculateDiscount(double subtotal) {
        return subtotal * percentage;
    }
}
