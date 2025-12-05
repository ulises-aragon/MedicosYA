package com.aragon.medicosya.repository;

import com.aragon.medicosya.models.Prescription;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class PrescriptionRepository {
    private final FirebaseFirestore firestore;

    public PrescriptionRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void createPrescription(Prescription prescription, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> map = new HashMap<>();
        map.put("appointment", firestore.collection("appointments").document(prescription.getAppointmentId()));
        map.put("provider", firestore.collection("providers").document(prescription.getProviderId()));
        map.put("client", firestore.collection("users").document(prescription.getClientId()));
        map.put("medications", prescription.getMedications());
        map.put("body", prescription.getBody());
        map.put("createdAt", Timestamp.now());

        firestore.collection("prescriptions").add(map)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getPrescription(String prescriptionId, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        firestore.collection("prescriptions").document(prescriptionId).get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
