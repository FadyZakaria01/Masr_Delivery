package org.example;

public class OrderLoggerListener implements OrderStatusObserver{
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println("[LOG]: Order #" + order.getId() + " transitioned from "
                + oldStatus + " to " + newStatus);
    }
}
