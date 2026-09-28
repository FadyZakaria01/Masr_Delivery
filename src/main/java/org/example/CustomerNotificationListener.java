package org.example;

public class CustomerNotificationListener implements OrderStatusObserver{
    @Override
    public void onOrderStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println( order.getCustomer().getName() + "with order" + order.getId() + "new status: " + newStatus);
    }
}
