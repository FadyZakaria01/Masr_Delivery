package org.example;

public abstract class MenuItem {
    private  final int ItemId ;
    private  String name;
    private String category;
    private int preparationTime;
    private boolean available;

    public MenuItem(int itemId, String name, String category, int preparationTime, boolean available) {
        ItemId = itemId;
        this.name = name;
        this.category = category;
        this.preparationTime = preparationTime;
        this.available = available;
    }

    public int getItemId() {
        return ItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public abstract double calculatePrice(double quantityOrWeight);
}
