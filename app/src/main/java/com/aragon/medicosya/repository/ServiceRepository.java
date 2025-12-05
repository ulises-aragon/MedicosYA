package com.aragon.medicosya.repository;

import com.aragon.medicosya.models.Service;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {
    private final FirebaseFirestore firestore;

    public ServiceRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getServicesForProvider(String providerId, OnSuccessListener<List<Service>> onSuccess, OnFailureListener onFailure) {
        CollectionReference servicesRef = firestore.collection("providers").document(providerId).collection("services");
        servicesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Service> list = new ArrayList<>();
                    for (DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()) {
                        Service s = ds.toObject(Service.class);
                        if (s != null) {
                            s.setId(ds.getId());
                            list.add(s);
                        }
                    }
                    onSuccess.onSuccess(list);
                }).addOnFailureListener(onFailure);
    }

    public void getService(String providerId, String serviceId, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        firestore.collection("providers").document(providerId).collection("services").document(serviceId).get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
