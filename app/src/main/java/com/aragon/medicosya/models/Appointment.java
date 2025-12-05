package com.aragon.medicosya.models;

import com.aragon.medicosya.enums.AppointmentStatus;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

public class Appointment {
    @Exclude
    private String id;

    private DocumentReference client;
    private DocumentReference provider;
    private DocumentReference service;
    private String status;
    private String notesClient;
    private String notesProvider;
    private Payment payment;
    private Timestamp startAt;
    private Timestamp endAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;


    @Exclude
    private User clientObject;
    @Exclude
    private Provider providerObject;
    @Exclude
    private Service serviceObject;

    public Appointment() {}


    // getters y setters
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Exclude
    public AppointmentStatus getStatusEnum() {
        if (status == null) return null;
        try {
            return AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public void setStatusEnum(AppointmentStatus status) {
        this.status = status == null ? null : status.toString();
    }

    public DocumentReference getClient() { return client; }
    public void setClient(DocumentReference client) { this.client = client; }
    public DocumentReference getProvider() { return provider; }
    public void setProvider(DocumentReference provider) { this.provider = provider; }
    public DocumentReference getService() { return service; }
    public void setService(DocumentReference serviceId) { this.service = serviceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Exclude
    public User getClientObject() {
        return clientObject;
    }

    public void setClientObject(User client) {
        this.clientObject = client;
    }

    @Exclude
    public Provider getProviderObject() {
        return providerObject;
    }

    public void setProviderObject(Provider providerObject) {
        this.providerObject = providerObject;
    }

    @Exclude
    public Service getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Service serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getNotesClient() {
        return notesClient;
    }

    public void setNotesClient(String notesClient) {
        this.notesClient = notesClient;
    }

    public String getNotesProvider() {
        return notesProvider;
    }

    public void setNotesProvider(String notesProvider) {
        this.notesProvider = notesProvider;
    }

    public Timestamp getStartAt() { return startAt; }
    public void setStartAt(Timestamp startAt) { this.startAt = startAt; }
    public Timestamp getEndAt() { return endAt; }
    public void setEndAt(Timestamp endAt) { this.endAt = endAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
