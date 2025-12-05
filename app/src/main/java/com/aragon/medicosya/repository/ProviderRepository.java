package com.aragon.medicosya.repository;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProviderRepository {
    private final FirebaseFirestore firestore;

    public ProviderRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getProvider(String providerId, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        firestore.collection("providers").document(providerId).get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}