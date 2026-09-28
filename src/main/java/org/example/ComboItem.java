package org.example;

import java.util.List;

public class ComboItem extends MenuItem{

    private List<MenuItem> bundledItems;
    private double bundlePrice;

    public ComboItem(int id, String name, String category, int preparationTimeMinutes, boolean isAvailable, List<MenuItem> bundledItems, double bundlePrice) {
        super(id, name, category, preparationTimeMinutes, isAvailable);
        this.bundledItems = bundledItems;
        this.bundlePrice = bundlePrice;
    }


    @Override
    public double calculatePrice(double count) {
        return bundlePrice * count;
    }

    public double getBundlePrice() {
        return bundlePrice;
    }

    public List<MenuItem> getBundledItems() {
        return bundledItems;
    }
}
