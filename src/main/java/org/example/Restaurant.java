package org.example;

import java.util.*;

public class Restaurant {
    private final int id;
    private String name;
    private String district;
    private Set<String> cuisineCategories;
    private double rating;
    private boolean isOpen;
    private List<MenuItem> menu;

    public Restaurant(int id, String name, String district, Set<String> cuisineCategories, double rating, boolean isOpen) {
        this.id = id;
        this.name = name;
        this.district = district;
        this.cuisineCategories = cuisineCategories;
        this.rating = rating;
        this.isOpen = isOpen;
        this.menu = new LinkedList<>();
    }

    public void addMenuItem(MenuItem item) {
        menu.add(item);
    }
    public void removeMenuItem(MenuItem item) {
        menu.remove(item);
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDistrict() {
        return district;
    }
    public Set<String> getCuisineCategories() {
        return Collections.unmodifiableSet(cuisineCategories);
    }
    public double getRating() {
        return rating;
    }
    public boolean isOpen() {
        return isOpen;
    }

    public List<MenuItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    public void setOpen(boolean open) { isOpen = open; }
    public void setRating(double rating) { this.rating = rating; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
