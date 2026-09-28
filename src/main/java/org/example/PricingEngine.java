package org.example;

public class PricingEngine {
    public double  calculateBaseDeliveryFee(double distanceInKm){
        double baseFee = 15.0;
        if (distanceInKm > 3.0) {
            double extraDistance = distanceInKm - 3.0;
            baseFee += extraDistance * 3.0;
        }
        return baseFee;
    }

    public void calculateOrderTotal(Order order, double distanceInKm, PromotionStrategy promo) {
        double subtotal = 0.0;
        for (OrderLine lineItem:order.getLineItems()){
            subtotal+=lineItem.calculateSubtotal();
        }
        double rawDeliveryFee = calculateBaseDeliveryFee(distanceInKm);
        double discountRate = order.getCustomer().getLoyaltyTier().getDeliveryDiscountRate();
        double finalDeliveryFee = rawDeliveryFee * (1.0 - discountRate);
        double serviceFee = subtotal * 0.10;

        double discountAmount = 0.0;
        if (promo != null) {
            discountAmount = promo.calculateDiscount(subtotal);
        }

        double total = (subtotal - discountAmount) + finalDeliveryFee + serviceFee;

        order.setSubtotal(subtotal);
        order.setDeliveryFee(finalDeliveryFee);
        order.setServiceFee(serviceFee);
        order.setDiscountAmount(discountAmount);
        order.setTotal(total);
    }
}
