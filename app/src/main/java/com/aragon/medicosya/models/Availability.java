package com.aragon.medicosya.models;

import com.aragon.medicosya.enums.AppointmentStatus;
import com.aragon.medicosya.enums.AvailabilityType;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Availability {
    @Exclude
    private String id;
    private String type;
    private Integer weekday;           // 0-6 (domingo=0) -> usado si type == RECURRING
    private String from;               // "08:00"
    private String to;                 // "12:00"
    private String date;               // "2025-02-15" -> usado si type == EXCEPTION
    private Boolean isAvailable;       // true = disponible, false = bloqueado
    private Timestamp createdAt;

    public Availability() {}

    // getters y setters...
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Exclude
    public AvailabilityType getTypeEnum() {
        if (type == null) return null;
        try {
            return AvailabilityType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public void setTypeEnum(AvailabilityType type) { this.type = type == null ? null : type.toString(); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getWeekday() { return weekday; }
    public void setWeekday(Integer weekday) { this.weekday = weekday; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}