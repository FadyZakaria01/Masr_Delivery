package org.example;

public class FlatAmountDiscountStrategy implements PromotionStrategy{

    private final double amount;
    public FlatAmountDiscountStrategy(double amount) {
        this.amount = amount;
    }

    @Override
    public double calculateDiscount(double subtotal) {
        return Math.min(amount, subtotal);
    }
}
