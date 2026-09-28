package org.example;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReportingService {
    public double getTotalRevenueInDateRange(List<Order> orderList, LocalDateTime start, LocalDateTime end) {
        return orderList.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> !order.getPlacedAt().isBefore(start) && !order.getPlacedAt().isAfter(end))
                .mapToDouble(Order::getTotal)
                .sum();
    }

    public List<Restaurant> getTop5RestaurantsByRevenueInMonth(List<Order> orders, int year, int month) {

        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> order.getPlacedAt().getYear() == year && order.getPlacedAt().getMonthValue() == month)
                .collect(Collectors.groupingBy(Order::getRestaurant, Collectors.summingDouble(Order::getTotal)))
                .entrySet().stream()
                .sorted(Map.Entry.<Restaurant, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    public Map<String, Double> getAverageOrderValuePerDistrict(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(order -> order.getDeliveryAddress().getDistrict()
                        , Collectors.averagingDouble(Order::getTotal)));

    }

    public List<Restaurant> getTopRatedPopularRestaurants(List<Restaurant> restaurants, List<Order> orders) {
        Map<Restaurant, Long> completedOrdersCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(Order::getRestaurant, Collectors.counting()));

        return restaurants.stream()
                .filter(r -> r.getRating() > 4.5)
                .filter(r -> completedOrdersCount.getOrDefault(r, 0L) >= 20)
                .collect(Collectors.toList());
    }

    public Map<OrderStatus, Long> getOrderCountByStatus(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    public Optional<MenuItem> getMostFrequentlyOrderedMenuItem(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .flatMap(order -> order.getLineItems().stream())
                .collect(Collectors.groupingBy(OrderLine::getMenuItem, Collectors.summingDouble(OrderLine::getQuantityOrWeight)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public List<Order> getCustomerOrderHistory(List<Order> orders, Customer customer) {
        return orders.stream()
                .filter(order -> order.getCustomer().equals(customer))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();
    }

    public Optional<Integer> getPeakOrderingHour(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(o -> o.getPlacedAt().getHour(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public List<Customer> getInactiveCustomers(List<Customer> customers, List<Order> orders) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Set<Customer> activeCustomers = orders.stream()
                .filter(order -> order.getPlacedAt().isAfter(thirtyDaysAgo))
                .map(Order::getCustomer)
                .collect(Collectors.toSet());

        return customers.stream().filter(customer -> !activeCustomers.contains(customer))
                .collect(Collectors.toList());
    }
    public List<Restaurant> searchRestaurants(List<Restaurant> restaurants, Predicate<Restaurant> filterCondition) {
        return restaurants.stream()
                .filter(filterCondition)
                .collect(Collectors.toList());
    }

    public double getCustomerTotalSpent(List<Order> orders, Customer customer) {
        return orders.stream()
                .filter(order -> order.getCustomer().equals(customer))
                .filter(order -> order.getStatus()==OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotal)
                .sum();
    }
}
