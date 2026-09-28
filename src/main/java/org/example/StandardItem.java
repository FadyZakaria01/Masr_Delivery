package org.example;

public class StandardItem extends MenuItem{

    private double price;
    public StandardItem(int itemId, String name, String category, int preparationTime, boolean available, double price) {
        super(itemId, name, category, preparationTime, available);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public double calculatePrice(double count){
        return price*count;
    }
}
