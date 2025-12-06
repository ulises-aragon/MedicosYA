package com.aragon.medicosya.repository;

import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

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

    public void getUserFromReference(DocumentReference clientRef,
                          OnSuccessListener<User> onSuccess,
                          OnFailureListener onFailure) {
        clientRef.get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u != null) u.setUid(doc.getId());
                    onSuccess.onSuccess(u);
                })
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

    public void getAppointmentsForClientWithProvider(DocumentReference clientRef,
                                                     DocumentReference providerRef,
                                                     OnSuccessListener<List<Appointment>> onSuccess,
                                                     OnFailureListener onFailure) {
        firestore.collection("appointments")
                .whereEqualTo("client", clientRef)
                .whereEqualTo("provider", providerRef)
                .orderBy("startAt", Query.Direction.DESCENDING)
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
}