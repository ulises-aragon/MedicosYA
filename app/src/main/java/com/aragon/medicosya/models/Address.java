package com.aragon.medicosya.models;

import com.google.firebase.firestore.GeoPoint;
public class Address {
    private String city;
    private String street;
    private GeoPoint location;

    public Address() {}

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
