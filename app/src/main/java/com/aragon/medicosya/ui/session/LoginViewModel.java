package com.aragon.medicosya.ui.session;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aragon.medicosya.enums.AuthField;
import com.aragon.medicosya.models.User;
import com.aragon.medicosya.repository.AuthRepository;
import com.aragon.medicosya.repository.UserRepository;
import com.aragon.medicosya.util.Event;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> toastError = new MutableLiveData<>(null);
    private final MutableLiveData<Event<User>> navigateEvent = new MutableLiveData<>(null);
    private final MutableLiveData<Map<AuthField, String>> fieldErrors = new MutableLiveData<>(new HashMap<>());

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\S+@\\S+\\.\\S+$");
    private static final int MIN_PASSWORD = 6;

    public LoginViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getToastError() { return toastError; }
    public LiveData<Map<AuthField, String>> getFieldErrors() { return fieldErrors; }
    public LiveData<Event<User>> getNavigateEvent() { return navigateEvent; }

    public void attemptLogin(String email, String password) {
        Map<AuthField, String> errors = new HashMap<>();
        fieldErrors.setValue(errors);

        boolean ok = true;
        if (email == null || email.trim().isEmpty()) {
            errors.put(AuthField.EMAIL, "El correo es obligatorio");
            ok = false;
        } else if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.put(AuthField.EMAIL, "Correo no válido");
            ok = false;
        }

        if (password == null || password.isEmpty()) {
            errors.put(AuthField.PASSWORD, "La contraseña es obligatoria");
            ok = false;
        } else if (password.length() < MIN_PASSWORD) {
            errors.put(AuthField.PASSWORD, "La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
            ok = false;
        }

        fieldErrors.setValue(errors);

        if (!ok) {
            return;
        }

        login(email.trim(), password);
    }
    private void login(String email, String password) {
        loading.setValue(true);
        authRepository.loginWithEmail(email, password,
                authResult -> {
                    FirebaseUser fuser = authResult.getUser();
                    if (fuser == null) {
                        loading.setValue(false);
                        toastError.setValue(new Event<>("Error interno: usuario nulo"));
                        return;
                    }

                    String uid = fuser.getUid();
                    userRepository.getUserById(uid,
                            (DocumentSnapshot doc) -> {
                                loading.setValue(false);
                                if (doc.exists()) {
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        if (user.getUid() == null || user.getUid().isEmpty()) user.setUid(doc.getId());
                                        navigateEvent.setValue(new Event<>(user));
                                    } else {
                                        toastError.setValue(new Event<>("No se pudo mapear el usuario"));
                                    }
                                } else {
                                    toastError.setValue(new Event<>("Perfil no encontrado. Complete su registro."));
                                }
                            },
                            e -> {
                                loading.setValue(false);
                                toastError.setValue(new Event<>(e.getMessage()));
                            });

                },
                e -> {
                    loading.setValue(false);
                    toastError.setValue(new Event<>(e.getMessage()));
                });
    }

    public void loadCurrentUserIfExists() {
        FirebaseUser fuser = authRepository.getCurrentUser();
        if (fuser == null) {
            Log.i("LoginViewModel", "NULL");

            return;
        }

        loading.setValue(true);
        userRepository.getUserById(fuser.getUid(),
                (DocumentSnapshot doc) -> {
                    loading.setValue(false);
                    if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            if (u.getUid() == null || u.getUid().isEmpty()) u.setUid(doc.getId());
                            navigateEvent.setValue(new Event<>(u));
                        } else {
                            toastError.setValue(new Event<>("No se pudo mapear el usuario."));
                        }
                    } else {
                        toastError.setValue(new Event<>("Perfil no encontrado."));
                    }
                },
                e -> {
                    loading.setValue(false);
                    toastError.setValue(new Event<>(e.getMessage()));
                });
    }
}