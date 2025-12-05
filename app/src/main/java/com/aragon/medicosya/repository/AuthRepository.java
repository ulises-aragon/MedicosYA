package com.aragon.medicosya.repository;

import com.aragon.medicosya.enums.UserRole;
import com.aragon.medicosya.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void registerWithEmail(String name, String email, String password,
                                  OnSuccessListener<FirebaseUser> onSuccess,
                                  OnFailureListener onFailure) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        User userDocument = new User();
                        userDocument.setUid(user.getUid());
                        userDocument.setName(name);
                        userDocument.setEmail(email);
                        userDocument.setRoleEnum(UserRole.CLIENT);
                        userDocument.setCreatedAt(Timestamp.now());

                        firestore.collection("users").document(user.getUid()).set(userDocument)
                                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(user))
                                .addOnFailureListener(onFailure);
                    } else {
                        onFailure.onFailure(new Exception("Registro: usuario nulo"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void loginWithEmail(String email, String password,
                               OnSuccessListener<AuthResult> onSuccess,
                               OnFailureListener onFailure) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void logout() {
        firebaseAuth.signOut();
    }
}
