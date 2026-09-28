package org.example;

public class OrderLine {
    private MenuItem menuItem;
    private int quantityOrWeight;

    public OrderLine(MenuItem menuItem, int quantityOrWeight) {
        this.menuItem = menuItem;
        this.quantityOrWeight = quantityOrWeight;
    }

    public double calculateSubtotal() {
        return menuItem.calculatePrice(quantityOrWeight);
    }

    public int getQuantityOrWeight() {
        return quantityOrWeight;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }
}
