package org.example;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Order {
    private final List<OrderStatusObserver> observers = new ArrayList<>();
    private final int id;
    private Customer customer;
    private Restaurant restaurant;
    private Address deliveryAddress;
    private List<OrderLine> lineItems;
    private OrderStatus status;
    private LocalDateTime placedAt;

    private double subtotal;
    private double deliveryFee;
    private double serviceFee;
    private double discountAmount;
    private double total;

    private Order(Builder builder) {
        this.id = builder.id;
        this.customer = builder.customer;
        this.restaurant = builder.restaurant;
        this.deliveryAddress = builder.deliveryAddress;
        this.lineItems = new ArrayList<>(builder.lineItems);
        this.status = builder.status;
        this.placedAt = builder.placedAt;
    }

    public static class Builder {
        private int id;
        private Customer customer;
        private Restaurant restaurant;
        private Address deliveryAddress;
        private List<OrderLine> lineItems=new ArrayList<>();
        private OrderStatus status=OrderStatus.PLACED;
        private LocalDateTime placedAt=LocalDateTime.now();

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder restaurant(Restaurant restaurant) {
            this.restaurant = restaurant;
            return this;
        }

        public Builder deliveryAddress(Address deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder lineItems(List<OrderLine> lineItems) {
            if (lineItems != null) {
                this.lineItems = lineItems;
            }
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder placedAt(LocalDateTime placedAt) {
            this.placedAt = placedAt;
            return this;
        }

        public Order build() {
            if (customer == null || restaurant == null || deliveryAddress == null) {
                throw new IllegalStateException("Customer, restaurant, and address are mandatory elements.");
            }
            return new Order(this);
        }

    }
    public void registerObserver(OrderStatusObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public void removeObserver(OrderStatusObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(OrderStatus oldStatus, OrderStatus newStatus) {
        for (OrderStatusObserver observer : observers) {
            observer.onOrderStatusChanged(this, oldStatus, newStatus);
        }
    }
    public void addLineItem(OrderLine item) {
        lineItems.add(item);
    }

    public void removeLineItem(OrderLine item) {
        lineItems.remove(item);
    }

    public boolean updateStatus(OrderStatus newStatus) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DELIVERED) {
            return false;
        }

        if (newStatus == OrderStatus.CANCELLED && this.status == OrderStatus.OUT_FOR_DELIVERY) {
            return false;
        }
        OrderStatus old=this.status;
        this.status = newStatus;
        notifyObservers(old, newStatus);
        return true;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<OrderLine> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
