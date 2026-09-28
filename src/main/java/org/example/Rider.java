package org.example;

import java.util.Objects;

public class Rider {
    private final int id;
    private String name;
    private VehicleType vehicleType;
    private String currentDistrict;
    private boolean isAvailable;
    private int completedDeliveriesCount;
    private Order currentActiveOrder;

    public Rider(int id, String name, VehicleType vehicleType, String currentDistrict) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
        this.currentDistrict = currentDistrict;
        this.isAvailable = true;
        this.completedDeliveriesCount = 0;
        this.currentActiveOrder = null;
    }

    public boolean assignOrder(Order order) {
        if (!isAvailable || currentActiveOrder != null) {
            return false;
        }
        this.currentActiveOrder = order;
        this.isAvailable = false;
        return true;
    }

    public void completeOrder() {
        if (this.currentActiveOrder != null) {
            this.completedDeliveriesCount++;
            this.currentActiveOrder = null;
            this.isAvailable = true;
        }
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    public String getCurrentDistrict() {
        return currentDistrict;
    }
    public boolean isAvailable() {
        return isAvailable;
    }
    public int getCompletedDeliveriesCount() {
        return completedDeliveriesCount;
    }
    public Order getCurrentActiveOrder() {
        return currentActiveOrder;
    }

    public void setCurrentDistrict(String currentDistrict) {
        this.currentDistrict = currentDistrict;
    }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Rider rider = (Rider) o;
        return id == rider.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
