package com.aragon.medicosya.models;

import com.aragon.medicosya.enums.PaymentMethod;
import com.aragon.medicosya.enums.PaymentStatus;
import com.google.firebase.firestore.Exclude;

public class Payment {
    private long amountCents;
    private String method;
    private String status;

    public Payment() {};

    @Exclude
    public PaymentMethod getMethodEnum() {
        if (method == null) return null;
        try {
            return PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public void setMethodEnum(PaymentMethod method) {
        this.method = method == null ? null : method.toString();
    }

    @Exclude
    public PaymentStatus getStatusEnum() {
        if (status == null) return null;
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    public void setStatusEnum(PaymentStatus status) {
        this.status = status == null ? null : status.toString();
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
