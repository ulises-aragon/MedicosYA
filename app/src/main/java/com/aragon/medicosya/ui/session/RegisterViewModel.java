package com.aragon.medicosya.ui.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aragon.medicosya.enums.AuthField;
import com.aragon.medicosya.models.User;
import com.aragon.medicosya.repository.AuthRepository;
import com.aragon.medicosya.repository.UserRepository;
import com.aragon.medicosya.util.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository authRepo;
    private final UserRepository userRepo;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> toastError = new MutableLiveData<>(null);
    private final MutableLiveData<Event<User>> navigateEvent = new MutableLiveData<>(null);
    private final MutableLiveData<Map<AuthField, String>> fieldErrors = new MutableLiveData<>(new HashMap<>());

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\S+@\\S+\\.\\S+$");
    private static final int MIN_PASSWORD = 6;

    public RegisterViewModel() {
        authRepo = new AuthRepository();
        userRepo = new UserRepository();
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<Event<String>> getToastError() { return toastError; }
    public LiveData<Map<AuthField, String>> getFieldErrors() { return fieldErrors; }
    public LiveData<Event<User>> getNavigateEvent() { return navigateEvent; }

    public void attemptRegister(String name, String email, String password, String confirmPassword) {
        Map<AuthField, String> errors = new HashMap<>();
        fieldErrors.setValue(errors);

        boolean ok = true;
        if (name == null || name.trim().isEmpty()) {
            errors.put(AuthField.NAME, "El nombre es obligatorio");
            ok = false;
        }

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

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            errors.put(AuthField.CONFIRM_PASSWORD, "Confirme la contraseña");
            ok = false;
        } else if (!password.equals(confirmPassword)) {
            errors.put(AuthField.CONFIRM_PASSWORD, "Las contraseñas no coinciden");
            ok = false;
        }

        fieldErrors.setValue(errors);
        if (!ok) return;

        loading.setValue(true);
        authRepo.registerWithEmail(name.trim(), email.trim(), password,
                firebaseUser -> {
                    loading.setValue(false);
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        userRepo.getUserById(uid,
                                documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User u = documentSnapshot.toObject(User.class);
                                        if (u != null) {
                                            if (u.getUid() == null || u.getUid().isEmpty()) u.setUid(documentSnapshot.getId());
                                            navigateEvent.setValue(new Event<>(u));
                                        } else {
                                            toastError.setValue(new Event<>("Registro correcto, pero no se pudo cargar el perfil."));
                                        }
                                    } else {
                                        toastError.setValue(new Event<>("Perfil no encontrado tras registro."));
                                    }
                                },
                                e -> {
                                    toastError.setValue(new Event<>(e.getMessage()));
                                });
                    } else {
                        toastError.setValue(new Event<>("Registro fallido: usuario nulo."));
                    }
                },
                e -> {
                    loading.setValue(false);
                    toastError.setValue(new Event<>(e.getMessage()));
                });
    }

    public void clearFieldErrors() {
        fieldErrors.setValue(new HashMap<>());
    }
}
