package com.aragon.medicosya.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

import java.util.List;

public class Provider {
    @Exclude
    private String id;

    private String name;
    private DocumentReference user;
    private Address address;
    private List<String> specialities;
    private double rating;
    private Timestamp createdAt;

    @Exclude
    private User userObject;

    public Provider() {}

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    @Exclude
    public User getUserObject() { return userObject; }
    public void setUserObject(User userObject) { this.userObject = userObject; }

    public DocumentReference getUser() { return user; }
    public void setUser(DocumentReference user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public List<String> getSpecialities() { return specialities; }
    public void setSpecialities(List<String> specialities) { this.specialities = specialities; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}