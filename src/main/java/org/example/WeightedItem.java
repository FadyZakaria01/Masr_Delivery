package org.example;

public class WeightedItem extends MenuItem{
    private double pricePerKg;

    public WeightedItem(int id, String name, String category, int preparationTimeMinutes, boolean isAvailable, double pricePerKg) {
        super(id, name, category, preparationTimeMinutes, isAvailable);
        this.pricePerKg = pricePerKg;
    }

    @Override
    public double calculatePrice(double kilograms) {
        return pricePerKg * kilograms;
    }

    public double getPricePerKg() { return pricePerKg; }
}
