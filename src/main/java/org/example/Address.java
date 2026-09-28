package org.example;

public class Address {
    private String district;
    private String details;


    public Address(String district, String details) {
        this.district = district;
        this.details = details;
    }

    public String getDistrict() {
        return district;
    }
    public String getDetails() {
        return details;
    }

}
