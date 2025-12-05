package com.aragon.medicosya.models;

import com.google.firebase.firestore.Exclude;

public class Service {
    @Exclude
    private String id;

    private String name;
    private int duration;
    private long priceCents;
    private String description;
    private boolean active;


    public Service() {}


    // getters y setters
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public long getPriceCents() { return priceCents; }
    public void setPriceCents(long priceCents) { this.priceCents = priceCents; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}