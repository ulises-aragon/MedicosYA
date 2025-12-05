package com.aragon.medicosya.ui.client.appointments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsViewModel extends ViewModel {

    private final AppointmentRepository repo = new AppointmentRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<List<Appointment>> getAppointments() { return appointments; }
    public LiveData<String> getError() { return error; }

    public void loadAppointments() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            error.setValue("Usuario no autenticado");
            return;
        }
        loading.setValue(true);

        DocumentReference clientRef =
                FirebaseFirestore.getInstance().collection("users").document(uid);

        repo.getAppointmentsForClient(clientRef, list -> {
            loading.setValue(false);
            appointments.setValue(list);
        }, e -> {
            loading.setValue(false);
            error.setValue(e.getMessage());
        });
    }
}
