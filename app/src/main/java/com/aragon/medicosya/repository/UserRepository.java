package com.aragon.medicosya.repository;

import com.aragon.medicosya.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseFirestore firestore;

    public UserRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getUserById(String uid, OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updateUser(String uid, User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        DocumentReference ref = firestore.collection("users").document(uid);
        ref.set(user)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void saveUserProfile(String uid, User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firestore.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }
}