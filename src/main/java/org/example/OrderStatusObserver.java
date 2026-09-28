package org.example;

public interface OrderStatusObserver {
    void onOrderStatusChanged(Order order,OrderStatus oldStatus,OrderStatus newStatus);
}

