package com.aragon.medicosya.repository;


import com.aragon.medicosya.enums.AppointmentStatus;
import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.models.Service;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentRepository {
    private final FirebaseFirestore firestore;

    public AppointmentRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void createAppointment(final Appointment appointment,
                                       OnSuccessListener<DocumentReference> onSuccess,
                                       OnFailureListener onFailure) {
        Query q = firestore.collection("appointments")
                .whereEqualTo("provider", appointment.getProviderObject())
                .whereIn("status", java.util.Arrays.asList(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED));

        q.get().addOnSuccessListener(querySnapshot -> {
            boolean conflict = false;
            for (DocumentSnapshot ds : querySnapshot.getDocuments()) {
                Timestamp existingStart = ds.getTimestamp("startAt");
                Timestamp existingEnd = ds.getTimestamp("endAt");
                if (existingStart != null && existingEnd != null) {
                    long newStart = appointment.getStartAt().toDate().getTime();
                    long newEnd = appointment.getEndAt().toDate().getTime();
                    long exStart = existingStart.toDate().getTime();
                    long exEnd = existingEnd.toDate().getTime();

                    if (newStart < exEnd && newEnd > exStart) {
                        conflict = true;
                        break;
                    }
                }
            }

            if (conflict) {
                onFailure.onFailure(new Exception("Conflicto: el proveedor ya tiene una cita en ese rango."));
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("client", appointment.getClient());
            map.put("provider", appointment.getProvider());
            map.put("service", appointment.getService());
            map.put("status", appointment.getStatus());
            map.put("notesProvider", appointment.getNotesProvider());
            map.put("notesClient", appointment.getNotesClient());
            map.put("payment", appointment.getPayment());
            map.put("startAt", appointment.getStartAt());
            map.put("endAt", appointment.getEndAt());
            map.put("createdAt", Timestamp.now());
            map.put("updatedAt", Timestamp.now());

            firestore.collection("appointments").add(map)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure);

        }).addOnFailureListener(onFailure);
    }

    public void getAppointmentsForClient(DocumentReference client, OnSuccessListener<List<Appointment>> onSuccess, OnFailureListener onFailure) {
        firestore.collection("appointments")
                .whereEqualTo("client", client)
                .orderBy("startAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        onSuccess.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<Task<Void>> tasks = new ArrayList<>();
                    List<Appointment> appointmentList = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Crear una cita
                        Appointment appointment = doc.toObject(Appointment.class);

                        if (appointment == null) continue;
                        if (appointment.getStatusEnum() == AppointmentStatus.CANCELLED) continue;
                        if (appointment.getStatusEnum() == AppointmentStatus.COMPLETED) continue;
                        if (appointment.getStatusEnum() == AppointmentStatus.NO_SHOW) continue;
                        appointment.setId(doc.getId());
                        appointmentList.add(appointment);

                        if (appointment.getProvider() != null) {
                            Task<Void> providerTask = appointment.getProvider().get().onSuccessTask(providerDoc -> {
                                if (providerDoc.exists()) {
                                    appointment.setProviderObject(providerDoc.toObject(Provider.class));
                                }
                                return Tasks.forResult(null);
                            });
                            tasks.add(providerTask);
                        }

                        if (appointment.getService() != null) {
                            Task<Void> serviceTask = appointment.getService().get().onSuccessTask(serviceDoc -> {
                                if (serviceDoc.exists()) {
                                    appointment.setServiceObject(serviceDoc.toObject(Service.class));
                                }
                                return Tasks.forResult(null);
                            });
                            tasks.add(serviceTask);
                        }
                    }

                    Tasks.whenAll(tasks).addOnSuccessListener(aVoid -> {
                        onSuccess.onSuccess(appointmentList);
                    }).addOnFailureListener(onFailure);
                }).addOnFailureListener(onFailure);
    }

    public ListenerRegistration listenAppointmentsForClient(DocumentReference client, EventListener<QuerySnapshot> listener) {
        return firestore.collection("appointments")
                .whereEqualTo("client", client)
                .orderBy("startAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public void cancelAppointment(String appointmentId,
                                  OnSuccessListener<Void> onSuccess,
                                  OnFailureListener onFailure) {
        updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELLED, onSuccess, onFailure);
    }

    // citas del proveedor
    public void getAppointmentsForProvider(DocumentReference providerRef,
                                           OnSuccessListener<List<Appointment>> onSuccess,
                                           OnFailureListener onFailure) {
        firestore.collection("appointments")
                .whereEqualTo("provider", providerRef)
                .orderBy("startAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Task<Void>> tasks = new ArrayList<>();
                    List<Appointment> appointmentList = new ArrayList<>();

                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        // Crear una cita
                        Appointment appointment = doc.toObject(Appointment.class);

                        if (appointment == null) continue;
                        if (appointment.getStatusEnum() == AppointmentStatus.CANCELLED) continue;
                        if (appointment.getStatusEnum() == AppointmentStatus.COMPLETED) continue;
                        if (appointment.getStatusEnum() == AppointmentStatus.NO_SHOW) continue;
                        appointment.setId(doc.getId());
                        appointmentList.add(appointment);

                        if (appointment.getProvider() != null) {
                            Task<Void> providerTask = appointment.getProvider().get().onSuccessTask(providerDoc -> {
                                if (providerDoc.exists()) {
                                    appointment.setProviderObject(providerDoc.toObject(Provider.class));
                                }
                                return Tasks.forResult(null);
                            });
                            tasks.add(providerTask);
                        }

                        if (appointment.getService() != null) {
                            Task<Void> serviceTask = appointment.getService().get().onSuccessTask(serviceDoc -> {
                                if (serviceDoc.exists()) {
                                    appointment.setServiceObject(serviceDoc.toObject(Service.class));
                                }
                                return Tasks.forResult(null);
                            });
                            tasks.add(serviceTask);
                        }
                    }

                    Tasks.whenAll(tasks).addOnSuccessListener(aVoid -> {
                        onSuccess.onSuccess(appointmentList);
                    }).addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // citas del proveedor filtradas por estado (para “solicitudes”)
    public void getPendingAppointmentsForProvider(DocumentReference providerRef,
                                                  OnSuccessListener<List<Appointment>> onSuccess,
                                                  OnFailureListener onFailure) {
        firestore.collection("appointments")
                .whereEqualTo("provider", providerRef)
                .whereEqualTo("status", AppointmentStatus.REQUESTED.toString())
                .orderBy("startAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Appointment> list = qs.toObjects(Appointment.class);
                    for (int i = 0; i < qs.size(); i++) {
                        list.get(i).setId(qs.getDocuments().get(i).getId());
                    }
                    onSuccess.onSuccess(list);
                })
                .addOnFailureListener(onFailure);
    }

    // actualizar estado (confirmar, cancelar, completar, etc.)
    public void updateAppointmentStatus(String appointmentId,
                                        AppointmentStatus newStatus,
                                        OnSuccessListener<Void> onSuccess,
                                        OnFailureListener onFailure) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus.toString());
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        firestore.collection("appointments")
                .document(appointmentId)
                .update(updates)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

}
