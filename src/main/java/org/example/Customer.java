package org.example;

import java.util.*;

public class Customer {
    private final int customerId;
    private String name;
    private String phone;
    private List<Address> addresses;
    private double balance;
    private  int completedOrderCount;
    private final Deque<String> searches = new ArrayDeque<>();

    public Customer(int customerId, String name, String mobileNumber, double walletBalance) {
        this.customerId = customerId;
        this.name = name;
        this.setMobileNumber(mobileNumber);
        this.balance = walletBalance;
        this.addresses = new ArrayList<>();
        this.completedOrderCount = 0;
    }

    public CustomerTier getLoyaltyTier() {
        if (completedOrderCount >= 30) {
            return CustomerTier.GOLD;
        } else if (completedOrderCount >= 10) {
            return CustomerTier.SILVER;
        }
        return CustomerTier.BRONZE;
    }

    public void addAddress(Address address) {
        addresses.add(address);
    }

    public void incrementCompletedOrderCount() {
        completedOrderCount++;
    }

    public void addSearch(String search) {
        searches.addFirst(search);

        if (searches.size() > 5) {
            searches.removeLast();
        }
    }
    public Deque<String> getSearches() {
        return new ArrayDeque<>(searches);
    }
    public int getCompletedOrderCount() {
        return completedOrderCount;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setMobileNumber(String mobileNumber) {
        if (mobileNumber != null && mobileNumber.matches("^(010|011|012|015)\\d{8}$")) {
            this.phone = mobileNumber;
        } else {
            throw new IllegalArgumentException("  mopile phone must started 010 , 011 , 012, 015");
        }
    }
    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(customerId);
    }
}
