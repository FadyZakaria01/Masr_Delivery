package org.example;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static java.nio.file.Files.readString;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final Map<Integer, Restaurant> restaurants = new HashMap<>();
    private static final Map<Integer, Customer> customers = new HashMap<>();
    private static final Map<Integer, Rider> riders = new HashMap<>();
    private static final Map<Integer, Order> orders = new HashMap<>();

    private static final Set<Integer> paidOrders = new HashSet<>();

    static PriorityQueue<Order> readyOrders =
            new PriorityQueue<>(
                    Comparator.comparing((Order o) -> tierPriority(o.getCustomer().getLoyaltyTier())).thenComparing(Order::getPlacedAt));

    private static final ReportingService reportingService = new ReportingService();
    private static final PricingEngine pricingEngine = new PricingEngine();
   public static void main() throws IOException {

       seedData();;

       while (true) {
           printMainMenu();

           int choice = readInt("Choose an option: ");

           switch (choice) {
               case 1 -> customerArea();
               case 2 -> restaurantArea();
               case 3 -> riderArea();
               case 4 -> adminAndReportsArea();
               case 0 -> {
                   System.out.println("Thank you for using Masr Delivery. Goodbye!");
                   return;
               }
               default -> System.out.println("Invalid choice.");
           }
       }
   }

    //Menu show
    private static void printMainMenu() {
        System.out.println("\n=============================================");
        System.out.println("      MASR DELIVERY SYSTEM - MAIN MENU       ");
        System.out.println("=============================================");
        System.out.println("1. Customer Area");
        System.out.println("2. Restaurant Area");
        System.out.println("3. Rider Area");
        System.out.println("4. Admin & Reports");
        System.out.println("0. Exit");
        System.out.println("=============================================");
    }
    // customer area
    private static void customerArea() throws IOException {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        Customer customer = selectCustomer();
        if (customer == null) return;

        while (true) {
            System.out.println("\n========== CUSTOMER AREA ==========");
            System.out.println("Logged in as: " + customer.getName());
            System.out.println("Tier: " + customer.getLoyaltyTier());
            System.out.println("Wallet: " + money(customer.getBalance()) + " EGP");
            System.out.println("-----------------------------------");
            System.out.println("1. Browse restaurants");
            System.out.println("2. Search restaurants");
            System.out.println("3. View restaurant menu");
            System.out.println("4. Place order");
            System.out.println("5. Pay for order");
            System.out.println("6. Track order");
            System.out.println("7. Cancel order");
            System.out.println("8. Order history");
            System.out.println("9. Last five searches");
            System.out.println("0. Back");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> browseRestaurants();
                case 2 -> searchRestaurants(customer);
                case 3 -> viewRestaurantMenu();
                case 4 -> placeOrder(customer);
                case 5 -> payOrder(customer);
                case 6 -> trackOrder(customer);
                case 7 -> cancelOrder(customer);
                case 8 -> showCustomerHistory(customer);
                case 9 -> showLastFiveSearches(customer);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void browseRestaurants() {
        List<Restaurant> result = restaurants.values()
                .stream()
                .filter(Restaurant::isOpen)
                .sorted(Comparator.comparing(Restaurant::getRating)
                        .reversed()
                        .thenComparing(Restaurant::getName))
                .toList();

        if (result.isEmpty()) {
            System.out.println("No open restaurants found.");
            return;
        }

        System.out.println("\n--- Open Restaurants ---");

        for (int i = 0; i < result.size(); i++) {
            Restaurant r = result.get(i);

            System.out.printf(
                    "%d. %s | ID: %d | District: %s | Cuisine: %s | Rating: %.1f%n",
                    i + 1,
                    r.getName(),
                    r.getId(),
                    r.getDistrict(),
                    r.getCuisineCategories(),
                    r.getRating()
            );
        }
    }
    private static void searchRestaurants(Customer customer) {
        String keyword = readNonEmptyString("Search by name or cuisine: ").toLowerCase();

        customer.addSearch(keyword);

        List<Restaurant> result = restaurants.values()
                .stream()
                .filter(r -> r.getName().toLowerCase().contains(keyword)
                                || r.getCuisineCategories()
                                .stream()
                                .anyMatch(c -> c.toLowerCase().contains(keyword)))
                .sorted(Comparator.comparing(Restaurant::getRating).reversed()).toList();

        if (result.isEmpty()) {
            System.out.println("Nothing found.");
            return;
        }

        System.out.println("\n--- Search Results ---");
        result.forEach(r -> System.out.println(r.getId() + " - " + r.getName() + " | " + r.getCuisineCategories() + " | Rating: " + r.getRating()));
    }
    private static void viewRestaurantMenu() {
        Restaurant restaurant = selectRestaurant();
        if (restaurant == null) return;

        System.out.println("\n========== " + restaurant.getName() + " MENU ==========");

        List<MenuItem> menu = restaurant.getMenu();

        if (menu.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);

            String price;

            if (item instanceof WeightedItem weightedItem) {
                price = money(weightedItem.getPricePerKg()) + " EGP/kg";
            } else if (item instanceof ComboItem comboItem) {
                price = money(comboItem.getBundlePrice()) + " EGP";
            } else if (item instanceof StandardItem standardItem) {
                price = money(standardItem.getPrice()) + " EGP";
            } else {
                price = "N/A";
            }

            System.out.printf("%d. %s | Category: %s | Price: %s | %s%n", i + 1, item.getName(), item.getCategory(), price, item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE");
        }
    }
    private static void placeOrder(Customer customer) throws IOException {
        Restaurant restaurant = selectRestaurant();
        if (restaurant == null) return;

        if (!restaurant.isOpen()) {
            System.out.println("Cannot place order: restaurant is closed.");
            return;
        }

        if (restaurant.getMenu().isEmpty()) {
            System.out.println("Restaurant has no menu items.");
            return;
        }

        Address address = selectCustomerAddress(customer);
        if (address == null) return;

        List<OrderLine> lines = new ArrayList<>();

        while (true) {
            viewRestaurantMenu();

            int itemNumber = readInt("Enter menu item number (0 = finish): ");

            if (itemNumber == 0) break;

            if (itemNumber < 1 || itemNumber > restaurant.getMenu().size()) {
                System.out.println("Invalid menu item.");
                continue;
            }

            MenuItem item = restaurant.getMenu().get(itemNumber - 1);

            if (!item.isAvailable()) {
                System.out.println("This item is unavailable.");
                continue;
            }

            int quantity = readInt("Enter quantity/weight: ");

            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                continue;
            }

            lines.add(new OrderLine(item, quantity));
            System.out.println("Item added.");
        }

        if (lines.isEmpty()) {
            System.out.println("Order must contain at least one item.");
            return;
        }

        int orderId = nextOrderId();

        Order order = new Order.Builder()
                .id(orderId)
                .customer(customer)
                .restaurant(restaurant)
                .deliveryAddress(address)
                .lineItems(lines)
                .build();

        order.registerObserver(new CustomerNotificationListener());
        order.registerObserver(new OrderLoggerListener());

        double distance = readDouble(
                "Enter distance between restaurant and delivery district (km): ");

        if (distance < 0) {
            System.out.println("Distance cannot be negative.");
            return;
        }

        PromotionStrategy promotion = choosePromotion();

        pricingEngine.calculateOrderTotal(order, distance, promotion);

        orders.put(orderId, order);

        System.out.println("\n========== ORDER SUMMARY ==========");
        printOrderSummary(order);

        System.out.println("Order created successfully.");
        System.out.println("Order reference: #" + order.getId());

        System.out.println("Status: " + order.getStatus());
    }
    private static PromotionStrategy choosePromotion() throws IOException {
        String code = readString(Path.of("Promotion code (press Enter for none): "))
                .trim()
                .toUpperCase();

        if (code.isEmpty()) {
            return new NoDiscountStrategy();
        }
        if (code.equals("NILE20")) {
            return subtotal -> Math.min(subtotal * 0.20, 50.0);
        }

        System.out.println("Invalid promotion code. No promotion applied.");
        return new NoDiscountStrategy();
    }
    private static void printOrderSummary(Order order) {
        System.out.printf("Subtotal       : %.2f EGP%n", order.getSubtotal());
        System.out.printf("Delivery fee   : %.2f EGP%n", order.getDeliveryFee());
        System.out.printf("Service fee    : %.2f EGP%n", order.getServiceFee());
        System.out.printf("Promotion      : %.2f EGP%n", order.getDiscountAmount());
        System.out.printf("TOTAL          : %.2f EGP%n", order.getTotal());
    }
    private static void payOrder(Customer customer) {
        Order order = selectCustomerOrder(customer);

        if (order == null) return;

        if (paidOrders.contains(order.getId())) {
            System.out.println("This order has already been paid.");
            return;
        }

        double required = order.getTotal();
        double available = customer.getBalance();

        if (available < required) {
            throw new InsufficientWalletBalanceException(required, available);
        }

        customer.setBalance(available - required);
        paidOrders.add(order.getId());

        System.out.println("Payment successful.");
        System.out.printf("New balance: %.2f EGP%n", customer.getBalance());
    }
    private static void trackOrder(Customer customer) {
        Order order = selectCustomerOrder(customer);

        if (order == null) return;

        long minutes = java.time.Duration.between(order.getPlacedAt(), LocalDateTime.now()).toMinutes();
        System.out.println("\nOrder #" + order.getId());
        System.out.println("Restaurant: " + order.getRestaurant().getName());
        System.out.println("Status: " + order.getStatus());
        System.out.println("Elapsed time: " + minutes + " minutes");
    }
    private static void cancelOrder(Customer customer) {
        Order order = selectCustomerOrder(customer);

        if (order == null) return;

        if (!canCancel(order.getStatus())) {
            System.out.println("Cancellation is not permitted for status: " + order.getStatus());
            return;
        }

        boolean updated = order.updateStatus(OrderStatus.CANCELLED);

        if (!updated) {
            System.out.println("Cancellation failed.");
            return;
        }

        if (paidOrders.remove(order.getId())) {
            customer.setBalance(customer.getBalance() + order.getTotal());
            System.out.println("Refunded: " + money(order.getTotal()) + " EGP");
        }

        System.out.println("Order cancelled successfully.");
    }
    private static void showCustomerHistory(Customer customer) {
        List<Order> history = reportingService.getCustomerOrderHistory(new ArrayList<>(orders.values()), customer);

        if (history.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        System.out.println("\n========== ORDER HISTORY ==========");

        history.forEach(order -> System.out.printf(
                "#%d | %s | %s | %.2f EGP | %s%n",
                order.getId(),
                order.getRestaurant().getName(),
                order.getPlacedAt(),
                order.getTotal(),
                order.getStatus()
        ));

        double totalSpent = reportingService.getCustomerTotalSpent(new ArrayList<>(orders.values()), customer);

        System.out.printf("Lifetime total spent: %.2f EGP%n", totalSpent);
    }
    private static void showLastFiveSearches(Customer customer) {
        Deque<String> searches = customer.getSearches();

        if (searches.isEmpty()) {
            System.out.println("No searches yet.");
            return;
        }

        System.out.println("\nLast five searches:");
        int i = 1;

        for (String search : searches) {
            System.out.println(i++ + ". " + search);
        }
    }
    //Restaurant area
    private static void restaurantArea() {
        Restaurant restaurant = selectRestaurant();

        if (restaurant == null) return;

        while (true) {
            System.out.println("\n========== RESTAURANT AREA ==========");
            System.out.println("Restaurant: " + restaurant.getName());
            System.out.println("Open: " + restaurant.isOpen());
            System.out.println("--------------------------------------");
            System.out.println("1. Accept pending order");
            System.out.println("2. Reject pending order");
            System.out.println("3. Mark order PREPARING");
            System.out.println("4. Mark order READY");
            System.out.println("5. Toggle restaurant open/closed");
            System.out.println("6. Toggle menu item availability");
            System.out.println("7. Add menu item");
            System.out.println("8. Remove menu item");
            System.out.println("9. View today's orders");
            System.out.println("10. View today's revenue");
            System.out.println("0. Back");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> acceptOrder(restaurant);
                case 2 -> rejectOrder(restaurant);
                case 3 -> changeRestaurantOrderStatus(
                        restaurant, OrderStatus.PREPARING);
                case 4 -> changeRestaurantOrderStatus(
                        restaurant, OrderStatus.READY);
                case 5 -> {
                    restaurant.setOpen(!restaurant.isOpen());
                    System.out.println(restaurant.isOpen() ? "Restaurant is now OPEN." : "Restaurant is now CLOSED.");
                }
                case 6 -> toggleItemAvailability(restaurant);
                case 7 -> addMenuItem(restaurant);
                case 8 -> removeMenuItem(restaurant);
                case 9 -> showRestaurantOrders(restaurant);
                case 10 -> showRestaurantRevenue(restaurant);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void acceptOrder(Restaurant restaurant) {
        Order order = selectRestaurantPendingOrder(restaurant);

        if (order == null) return;

        if (order.getStatus() != OrderStatus.PLACED) {
            System.out.println("Only PLACED orders can be accepted.");
            return;
        }

        changeStatus(order, OrderStatus.ACCEPTED);
    }
    private static void rejectOrder(Restaurant restaurant) {
        Order order = selectRestaurantPendingOrder(restaurant);

        if (order == null) return;

        if (!canCancel(order.getStatus())) {
            System.out.println("This order cannot be cancelled.");
            return;
        }

        changeStatus(order, OrderStatus.CANCELLED);

        if (paidOrders.remove(order.getId())) {
            Customer customer = order.getCustomer();
            customer.setBalance(customer.getBalance() + order.getTotal());
            System.out.println("Customer refunded.");
        }
    }
    private static void changeRestaurantOrderStatus(Restaurant restaurant, OrderStatus newStatus) {
        List<Order> restaurantOrders = orders.values()
                .stream()
                .filter(o -> o.getRestaurant().equals(restaurant))
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED)
                .toList();

        if (restaurantOrders.isEmpty()) {
            System.out.println("No active orders.");
            return;
        }

        restaurantOrders.forEach(o ->
                System.out.println("#" + o.getId() + " | " + o.getStatus() + " | Customer: " + o.getCustomer().getName()));

        int id = readInt("Order ID: ");
        Order order = orders.get(id);

        if (order == null || !order.getRestaurant().equals(restaurant)) {
            System.out.println("Order not found.");
            return;
        }

        if (!isLegalTransition(order.getStatus(), newStatus)) {
            System.out.println("Illegal status transition: " + order.getStatus() + " -> " + newStatus);
            return;
        }
        changeStatus(order, newStatus);

        if (newStatus == OrderStatus.READY) {
            readyOrders.offer(order);
            System.out.println("Order added to READY queue.");
        }
    }
    private static void toggleItemAvailability(Restaurant restaurant) {
        List<MenuItem> menu = restaurant.getMenu();

        if (menu.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " | Available: " + item.isAvailable());
        }

        int index = readInt("Item number: ") - 1;

        if (index < 0 || index >= menu.size()) {
            System.out.println("Invalid item.");
            return;
        }

        MenuItem item = menu.get(index);
        item.setAvailable(!item.isAvailable());

        System.out.println(item.getName() + " is now " + (item.isAvailable() ? "AVAILABLE" : "UNAVAILABLE"));
    }
    private static void addMenuItem(Restaurant restaurant) {
        int id = readInt("Item ID: ");
        String name = readNonEmptyString("Item name: ");
        String category = readNonEmptyString("Category: ");
        int preparationTime = readInt("Preparation time (minutes): ");
        double price = readDouble("Price: ");

        if (price <= 0 || preparationTime < 0) {
            System.out.println("Invalid price or preparation time.");
            return;
        }

        System.out.println("1. Standard");
        System.out.println("2. Weighted");
        System.out.println("3. Combo");

        int type = readInt("Item type: ");

        MenuItem item;

        switch (type) {
            case 1 -> item = new StandardItem(id, name, category, preparationTime, true, price);

            case 2 -> item = new WeightedItem(id, name, category, preparationTime, true, price);

            case 3 -> {
                item = new ComboItem(id, name, category, preparationTime, true, new ArrayList<>(), price);
            }
            default -> {
                System.out.println("Invalid type.");
                return;
            }
        }

        restaurant.addMenuItem(item);
        System.out.println("Menu item added successfully.");
    }
    private static void removeMenuItem(Restaurant restaurant) {
        List<MenuItem> menu = restaurant.getMenu();

        if (menu.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        for (int i = 0; i < menu.size(); i++) {
            System.out.println((i + 1) + ". " + menu.get(i).getName());
        }

        int index = readInt("Item number: ") - 1;

        if (index < 0 || index >= menu.size()) {
            System.out.println("Invalid item.");
            return;
        }

        restaurant.removeMenuItem(menu.get(index));
        System.out.println("Item removed.");
    }
    private static void showRestaurantOrders(Restaurant restaurant) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();

        orders.values()
                .stream()
                .filter(o -> o.getRestaurant().equals(restaurant))
                .filter(o -> !o.getPlacedAt().isBefore(start))
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .forEach(o -> System.out.println("#" + o.getId() + " | " + o.getStatus() + " | " + o.getTotal() + " EGP"));
    }
    private static void showRestaurantRevenue(Restaurant restaurant) {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        double revenue = orders.values()
                .stream()
                .filter(o -> o.getRestaurant().equals(restaurant))
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> !o.getPlacedAt().isBefore(start)
                        && !o.getPlacedAt().isAfter(end))
                .mapToDouble(Order::getTotal)
                .sum();

        System.out.printf("Today's revenue: %.2f EGP%n", revenue);
    }
    //Rider area
    private static void riderArea() {
        Rider rider = selectRider();
        if (rider == null) return;

        while (true) {
            System.out.println("\n========== RIDER AREA ==========");
            System.out.println("Rider: " + rider.getName());
            System.out.println("Vehicle: " + rider.getVehicleType());
            System.out.println("District: " + rider.getCurrentDistrict());
            System.out.println("Available: " + rider.isAvailable());
            System.out.println("Completed deliveries: " + rider.getCompletedDeliveriesCount());
            System.out.println("--------------------------------");
            System.out.println("1. Go ON duty");
            System.out.println("2. Go OFF duty");
            System.out.println("3. Assign next READY order");
            System.out.println("4. View current order");
            System.out.println("5. Mark order picked up");
            System.out.println("6. Mark order delivered");
            System.out.println("7. Statistics");
            System.out.println("0. Back");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> rider.setAvailable(true);
                case 2 -> {
                    if (rider.getCurrentActiveOrder() != null) {
                        System.out.println("Cannot go off duty while handling an order.");
                    } else {
                        rider.setAvailable(false);
                        System.out.println("Rider is OFF duty.");
                    }
                }
                case 3 -> assignNextReadyOrder(rider);
                case 4 -> showCurrentRiderOrder(rider);
                case 5 -> markPickedUp(rider);
                case 6 -> markDelivered(rider);
                case 7 -> System.out.println("Completed deliveries: " + rider.getCompletedDeliveriesCount());
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void assignNextReadyOrder(Rider rider) {
        if (!rider.isAvailable() || rider.getCurrentActiveOrder() != null) {
            throw new RiderBusyException(rider.getName());
        }

        Order order = findNextCompatibleReadyOrder(rider);

        if (order == null) {
            System.out.println("No READY order is currently available.");
            return;
        }

        if (!rider.assignOrder(order)) {
            throw new RiderBusyException(rider.getName());
        }

        changeStatus(order, OrderStatus.ASSIGNED);

        System.out.println(
                "Order #" + order.getId()
                        + " assigned to " + rider.getName()
        );
    }
    private static Order findNextCompatibleReadyOrder(Rider rider) {
        if (readyOrders.isEmpty()) return null;

        List<Order> candidates = new ArrayList<>(readyOrders);

        candidates.sort(
                Comparator.comparing((Order o) -> tierPriority(o.getCustomer().getLoyaltyTier())).thenComparing(Order::getPlacedAt));

        for (Order order : candidates) {
            if (isVehicleEligible(rider, order)) {
                readyOrders.remove(order);
                return order;
            }
        }

        return null;
    }
    private static boolean isVehicleEligible(Rider rider, Order order) {
        return rider.getVehicleType() == VehicleType.MOTORCYCLE
                || rider.getVehicleType() == VehicleType.BICYCLE
                || rider.getVehicleType() == VehicleType.CAR;
    }
    private static void showCurrentRiderOrder(Rider rider) {
        Order order = rider.getCurrentActiveOrder();

        if (order == null) {
            System.out.println("No active order.");
            return;
        }
        System.out.println("Order #" + order.getId());
        System.out.println("Customer: " + order.getCustomer().getName());
        System.out.println("Restaurant: " + order.getRestaurant().getName());
        System.out.println("Address: " + order.getDeliveryAddress().getDistrict() + " - " + order.getDeliveryAddress().getDetails());System.out.println("Status: " + order.getStatus());
    }
    private static void markPickedUp(Rider rider) {
        Order order = rider.getCurrentActiveOrder();

        if (order == null) {
            System.out.println("No active order.");
            return;
        }

        if (order.getStatus() != OrderStatus.ASSIGNED) {
            System.out.println("Order must be ASSIGNED first.");
            return;
        }

        changeStatus(order, OrderStatus.OUT_FOR_DELIVERY);
        System.out.println("Order picked up.");
    }
    private static void markDelivered(Rider rider) {
        Order order = rider.getCurrentActiveOrder();

        if (order == null) {
            System.out.println("No active order.");
            return;
        }

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            System.out.println("Order must be OUT_FOR_DELIVERY first.");
            return;
        }

        changeStatus(order, OrderStatus.DELIVERED);

        order.getCustomer().incrementCompletedOrderCount();
        rider.completeOrder();
        System.out.println("Order delivered successfully.");
    }
    // ADMIN + REPORTS
    private static void adminAndReportsArea() {
        while (true) {
            System.out.println("\n========== ADMIN & REPORTS ==========");
            System.out.println("1. Add restaurant");
            System.out.println("2. Remove restaurant");
            System.out.println("3. Add customer");
            System.out.println("4. Add rider");
            System.out.println("5. Create promotion demo");
            System.out.println("6. Platform statistics");
            System.out.println("7. Reports");
            System.out.println("0. Back");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> addRestaurant();
                case 2 -> removeRestaurant();
                case 3 -> addCustomer();
                case 4 -> addRider();
                case 5 -> createPromotionDemo();
                case 6 -> showPlatformStatistics();
                case 7 -> reportsMenu();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void addRestaurant() {
        int id = readInt("Restaurant ID: ");

        if (restaurants.containsKey(id)) {
            System.out.println("ID already exists.");
            return;
        }

        String name = readNonEmptyString("Name: ");
        String district = readNonEmptyString("District: ");
        double rating = readDouble("Rating (0 - 5): ");

        if (rating < 0 || rating > 5) {
            System.out.println("Rating must be between 0 and 5.");
            return;
        }
        Set<String> cuisines = new HashSet<>();
        int count = readInt("Number of cuisine categories: ");
        for (int i = 0; i < count; i++) {
            cuisines.add(readNonEmptyString("Cuisine " + (i + 1) + ": "));
        }
        Restaurant restaurant = new Restaurant(id, name, district, cuisines, rating, true);
        restaurants.put(id, restaurant);
        System.out.println("Restaurant added.");
    }
    private static void removeRestaurant() {
        Restaurant restaurant = selectRestaurant();
        if (restaurant == null) return;
        restaurants.remove(restaurant.getId());
        System.out.println("Restaurant removed.");
    }
    private static void addCustomer() {
        int id = readInt("Customer ID: ");
        if (customers.containsKey(id)) {
            System.out.println("Customer ID already exists.");
            return;
        }
        String name = readNonEmptyString("Name: ");
        String phone = readNonEmptyString("Egyptian mobile number: ");
        double balance = readDouble("Wallet balance: ");

        if (balance < 0) {
            System.out.println("Balance cannot be negative.");
            return;
        }
        try {
            Customer customer = new Customer(id, name, phone, balance);
            int addressCount = readInt("Number of addresses: ");

            for (int i = 0; i < addressCount; i++) {
                String district = readNonEmptyString("District: ");
                String details = readNonEmptyString("Address details: ");

                customer.addAddress(new Address(district, details));
            }
            customers.put(id, customer);
            System.out.println("Customer added.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void addRider() {
        int id = readInt("Rider ID: ");
        if (riders.containsKey(id)) {
            System.out.println("Rider ID already exists.");
            return;
        }
        String name = readNonEmptyString("Name: ");
        String district = readNonEmptyString("Current district: ");

        System.out.println("1. MOTORCYCLE");
        System.out.println("2. BICYCLE");
        System.out.println("3. CAR");
        int type = readInt("Vehicle: ");
        VehicleType vehicleType;
        switch (type) {
            case 1 -> vehicleType = VehicleType.MOTORCYCLE;
            case 2 -> vehicleType = VehicleType.BICYCLE;
            case 3 -> vehicleType = VehicleType.CAR;
            default -> {
                System.out.println("Invalid vehicle.");
                return;
            }
        }
        riders.put(id, new Rider(id, name, vehicleType, district));
        System.out.println("Rider added.");
    }
    private static void createPromotionDemo() {
        System.out.println("\nPromotion examples:");
        System.out.println("NILE20 -> 20% off subtotal, maximum 50 EGP");
        System.out.println("FLAT50 -> 50 EGP off subtotal");
        String code = readNonEmptyString("Enter promotion code: ")
                .toUpperCase();
        double subtotal = readDouble("Test subtotal: ");
        double discount;
        switch (code) {
            case "NILE20" ->
                    discount = Math.min(subtotal * 0.20, 50.0);
            case "FLAT50" ->
                    discount = Math.min(subtotal, 50.0);
            default -> {
                System.out.println("Unknown promotion.");
                return;
            }
        }

        System.out.printf("Discount for %.2f EGP = %.2f EGP%n", subtotal, discount);
    }
    private static void showPlatformStatistics() {
        System.out.println("\n========== PLATFORM STATISTICS ==========");
        System.out.println("Restaurants: " + restaurants.size());
        System.out.println("Customers  : " + customers.size());
        System.out.println("Riders     : " + riders.size());
        System.out.println("Orders     : " + orders.size());

        Map<OrderStatus, Long> statusCount = reportingService.getOrderCountByStatus(new ArrayList<>(orders.values()));
        statusCount.forEach((status, count) -> System.out.println(status + " : " + count));
    }
    private static void reportsMenu() {
        while (true) {
            System.out.println("\n========== REPORTS ==========");
            System.out.println("1. Total revenue in date range");
            System.out.println("2. Top 5 restaurants by revenue");
            System.out.println("3. Average order value per district");
            System.out.println("4. Rating > 4.5 and 20+ completed orders");
            System.out.println("5. Orders grouped by status");
            System.out.println("6. Rider delivery statistics");
            System.out.println("7. Most frequently ordered menu item");
            System.out.println("8. Customer history and total spent");
            System.out.println("9. Peak ordering hour");
            System.out.println("10. Customers inactive for 30 days");
            System.out.println("0. Back");

            int choice = readInt("Choose: ");

            switch (choice) {
                case 1 -> reportTotalRevenue();
                case 2 -> reportTopRestaurants();
                case 3 -> reportAverageOrderValue();
                case 4 -> reportTopRatedRestaurants();
                case 5 -> reportOrdersByStatus();
                case 6 -> reportRiders();
                case 7 -> reportMostOrderedItem();
                case 8 -> reportCustomerHistory();
                case 9 -> reportPeakHour();
                case 10 -> reportInactiveCustomers();
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void reportTotalRevenue() {
        LocalDateTime start = readDateTime("Start (yyyy-MM-ddTHH:mm): ");
        LocalDateTime end = readDateTime("End (yyyy-MM-ddTHH:mm): ");
        double revenue = reportingService.getTotalRevenueInDateRange(new ArrayList<>(orders.values()), start, end);
        System.out.printf("Total revenue: %.2f EGP%n", revenue);
    }
    private static void reportTopRestaurants() {
        int year = readInt("Year: ");
        int month = readInt("Month: ");

        List<Restaurant> result = reportingService.getTop5RestaurantsByRevenueInMonth(new ArrayList<>(orders.values()), year, month);
        if (result.isEmpty()) {
            System.out.println("No delivered orders found.");
            return;
        }
        result.forEach(r -> System.out.println(r.getName() + " | Rating: " + r.getRating()));
    }
    private static void reportAverageOrderValue() {
        Map<String, Double> result = reportingService.getAverageOrderValuePerDistrict(new ArrayList<>(orders.values()));
        result.forEach((district, average) -> System.out.printf("%s -> %.2f EGP%n", district, average));
    }
    private static void reportTopRatedRestaurants() {
        List<Restaurant> result = reportingService.getTopRatedPopularRestaurants(new ArrayList<>(restaurants.values()), new ArrayList<>(orders.values()));

        if (result.isEmpty()) {
            System.out.println("No restaurants match the conditions.");
            return;
        }

        result.forEach(r -> System.out.println(r.getName() + " | Rating: " + r.getRating()));
    }
    private static void reportOrdersByStatus() {
        Map<OrderStatus, Long> result = reportingService.getOrderCountByStatus(new ArrayList<>(orders.values()));
        result.forEach((status, count) -> System.out.println(status + " -> " + count));
    }
    private static void reportRiders() {
        System.out.println("\nRider statistics:");

        riders.values()
                .stream()
                .sorted(Comparator.comparing(
                        Rider::getCompletedDeliveriesCount
                ).reversed())
                .forEach(r -> System.out.println(r.getName() + " | Completed deliveries: " + r.getCompletedDeliveriesCount() + " | Vehicle: " + r.getVehicleType()));

        System.out.println("Average delivery duration cannot be calculated with the " + "current Rider/Order model because no delivery-completion " + "timestamp is stored.");
    }
    private static void reportMostOrderedItem() {
        Optional<MenuItem> item = reportingService.getMostFrequentlyOrderedMenuItem(new ArrayList<>(orders.values()));

        if (item.isEmpty()) {
            System.out.println("No ordered menu item exists.");
            return;
        }
        System.out.println("Most frequently ordered item: " + item.get().getName());
    }
    private static void reportCustomerHistory() {
        Customer customer = selectCustomer();
        if (customer == null) return;
        showCustomerHistory(customer);
    }
    private static void reportPeakHour() {
        Optional<Integer> hour = reportingService.getPeakOrderingHour(new ArrayList<>(orders.values()));

        if (hour.isEmpty()) {
            System.out.println("No orders exist.");
            return;
        }
        System.out.println("Peak ordering hour: " + hour.get() + ":00");
    }
    private static void reportInactiveCustomers() {
        List<Customer> result = reportingService.getInactiveCustomers(new ArrayList<>(customers.values()), new ArrayList<>(orders.values()));
        if(result.isEmpty()) {
            System.out.println("No inactive customers.");
            return;
        }
        result.forEach(c -> System.out.println(c.getCustomerId() + " - " + c.getName()));
    }

    //  // STATUS / ORDER HELPERS
    private static void changeStatus(Order order, OrderStatus newStatus) {
        if (!isLegalTransition(order.getStatus(), newStatus)) {
            throw new IllegalOrderStatusTransitionException("Illegal transition: " + order.getStatus() + " -> " + newStatus);
        }
        if (!order.updateStatus(newStatus)) {
            throw new IllegalOrderStatusTransitionException("Could not change order status.");
        }
    }
    private static boolean isLegalTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED
                || current == OrderStatus.DELIVERED) {
            return false;
        }

        if (next == OrderStatus.CANCELLED) {
            return current != OrderStatus.OUT_FOR_DELIVERY;
        }

        return switch (current) {
            case PLACED -> next == OrderStatus.ACCEPTED;
            case ACCEPTED -> next == OrderStatus.PREPARING;
            case PREPARING -> next == OrderStatus.READY;
            case READY -> next == OrderStatus.ASSIGNED;
            case ASSIGNED -> next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED;
            default -> false;
        };
    }
    private static boolean canCancel(OrderStatus status) {
        return status == OrderStatus.PLACED
                || status == OrderStatus.ACCEPTED
                || status == OrderStatus.PREPARING
                || status == OrderStatus.READY
                || status == OrderStatus.ASSIGNED;
    }
    // SELECTION HELPERS
    private static Customer selectCustomer() {
        System.out.println("\n--- Customers ---");

        customers.values().forEach(c ->
                System.out.println(c.getCustomerId() + " - " + c.getName() + " | " + c.getLoyaltyTier()));
        int id = readInt("Customer ID: ");
        Customer customer = customers.get(id);

        if (customer == null) {
            System.out.println("Customer not found.");
        }
        return customer;
    }
    private static Restaurant selectRestaurant() {
        System.out.println("\n--- Restaurants ---");
        restaurants.values()
                .stream()
                .sorted(Comparator.comparing(Restaurant::getName))
                .forEach(r ->
                        System.out.println(r.getId() + " - " + r.getName() + " | " + r.getDistrict() + " | Rating: " + r.getRating() + " | " + (r.isOpen() ? "OPEN" : "CLOSED")));
        int id = readInt("Restaurant ID: ");
        Restaurant restaurant = restaurants.get(id);

        if (restaurant == null) {
            System.out.println("Restaurant not found.");
        }

        return restaurant;
    }
    private static Rider selectRider() {
        System.out.println("\n--- Riders ---");

        riders.values().forEach(r ->
                System.out.println(r.getId() + " - " + r.getName() + " | " + r.getVehicleType() + " | " + (r.isAvailable() ? "AVAILABLE" : "BUSY/OFF")));

        int id = readInt("Rider ID: ");
        Rider rider = riders.get(id);

        if (rider == null) {
            System.out.println("Rider not found.");
        }
        return rider;
    }
    private static Address selectCustomerAddress(Customer customer) {
        List<Address> addresses = customer.getAddresses();

        if (addresses.isEmpty()) {
            System.out.println("Customer has no saved addresses.");
            return null;
        }

        System.out.println("\n--- Addresses ---");

        for (int i = 0; i < addresses.size(); i++) {
            Address address = addresses.get(i);
            System.out.println((i + 1) + ". " + address.getDistrict() + " - " + address.getDetails());
        }
        int choice = readInt("Choose address: ") - 1;

        if (choice < 0 || choice >= addresses.size()) {
            System.out.println("Invalid address.");
            return null;
        }
        return addresses.get(choice);
    }
    private static Order selectCustomerOrder(Customer customer) {
        List<Order> customerOrders = orders.values()
                .stream()
                .filter(o -> o.getCustomer().equals(customer))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .toList();

        if (customerOrders.isEmpty()) {
            System.out.println("No orders found.");
            return null;
        }
        customerOrders.forEach(o ->
                System.out.println("#" + o.getId() + " | " + o.getRestaurant().getName() + " | " + o.getStatus() + " | " + money(o.getTotal()) + " EGP"));

        int id = readInt("Order ID: ");
        Order order = orders.get(id);

        if (order == null || !order.getCustomer().equals(customer)) {
            System.out.println("Order not found.");
            return null;
        }
        return order;
    }
    private static Order selectRestaurantPendingOrder(Restaurant restaurant) {
        List<Order> pending = orders.values()
                .stream()
                .filter(o -> o.getRestaurant().equals(restaurant))
                .filter(o -> o.getStatus() == OrderStatus.PLACED)
                .sorted(Comparator.comparing(Order::getPlacedAt))
                .toList();

        if (pending.isEmpty()) {
            System.out.println("No pending orders.");
            return null;
        }
        pending.forEach(o -> System.out.println("#" + o.getId() + " | Customer: " + o.getCustomer().getName()));
        int id = readInt("Order ID: ");
        Order order = orders.get(id);

        if (order == null || !order.getRestaurant().equals(restaurant)) {
            System.out.println("Order not found.");
            return null;
        }
        return order;
    }
    // input helper
    public static int readInt(String message) {
        while (true) {
            System.out.print(message);

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            }
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
        }
    }
    public static double readDouble(String message) {
        while (true) {
            System.out.print(message);

            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            }

            System.out.println("Invalid input. Please enter a valid number.");
            scanner.nextLine();
        }
    }
    public static String readNonEmptyString(String message) {
        while (true) {
            System.out.print(message);

            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Name cannot be empty.");
        }
    }
    private static LocalDateTime readDateTime(String message) {
        while (true) {
            try {
                return LocalDateTime.parse(readNonEmptyString(message));
            } catch (Exception e) {
                System.out.println("Invalid format. Example: 2026-09-25T15:30");
            }
        }
    }
    private static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
    private static int nextOrderId() {
        return orders.keySet()
                .stream()
                .max(Integer::compareTo).orElse(0) + 1;
    }
    private static int tierPriority(CustomerTier tier) {
        return switch (tier) {
            case GOLD -> 0;
            case SILVER -> 1;
            case BRONZE -> 2;
        };
    }
    // SAMPLE DATA
    private static void seedData() {
        if (!restaurants.isEmpty()) return;
        Restaurant r1 = new Restaurant(1, "Maadi Kitchen", "Maadi", new HashSet<>(Arrays.asList("Egyptian", "Grill")), 4.7, true);
        Restaurant r2 = new Restaurant(2, "Dokki Pizza", "Dokki", new HashSet<>(Arrays.asList("Pizza", "Italian")), 4.4, true);
        r1.addMenuItem(new StandardItem(101, "Kofta", "Grill", 20, true, 120));
        r1.addMenuItem(new WeightedItem(102, "Grilled Meat", "Grill", 30, true, 350));
        r2.addMenuItem(new StandardItem(201, "Margherita Pizza", "Pizza", 25, true, 150));
        restaurants.put(r1.getId(), r1);
        restaurants.put(r2.getId(), r2);
        Customer c1 = new Customer(1, "Fady", "01012345678", 1000);
        c1.addAddress(new Address("Faisal", "Main Faisal Street"));
        c1.addAddress(new Address("Dokki", "Dokki Main Street"));
        customers.put(c1.getCustomerId(), c1);
        riders.put(1, new Rider(1, "Ahmed", VehicleType.MOTORCYCLE, "Maadi"));
        riders.put(2, new Rider(2, "Mohamed", VehicleType.CAR, "Dokki"));
    }
}
