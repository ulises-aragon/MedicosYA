package com.aragon.medicosya.enums;

import android.graphics.Color;

import androidx.annotation.NonNull;

public enum AppointmentStatus {
    REQUESTED("PENDIENTE", Color.parseColor("#9B64D3")),
    CONFIRMED("AGENDADA", Color.parseColor("#009688")),
    PROGRAMMED("PROGRAMADA", Color.parseColor("#9B64D3")),
    CHECKED_IN("EN PROCESO", Color.parseColor("#009688")),
    COMPLETED("COMPLETADA", Color.parseColor("#009688")),
    CANCELLED("CANCELADA", Color.parseColor("#9B64D3")),
    NO_SHOW("IGNORADA", Color.parseColor("#9B64D3"));

    private final String label;
    private final int color;

    AppointmentStatus(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }
}
