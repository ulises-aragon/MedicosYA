package com.aragon.medicosya.ui.provider.appointments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.aragon.medicosya.databinding.FragmentAppointmentBottomSheetBinding;
import com.aragon.medicosya.enums.AppointmentStatus;
import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.models.Provider;
import com.aragon.medicosya.models.Service;
import com.aragon.medicosya.repository.AppointmentRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_APPOINTMENT_ID = "arg_appointment_id";

    private FragmentAppointmentBottomSheetBinding binding;
    private String appointmentId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final AppointmentRepository repo = new AppointmentRepository();

    private final DateTimeFormatter dateTimeFmt =
            DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", new Locale("es"));

    public interface Listener {
        void onAppointmentUpdated();
        void onOpenClient(String clientId);
    }

    private Listener listener;

    public static AppointmentBottomSheet newInstance(String appointmentId) {
        AppointmentBottomSheet s = new AppointmentBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_APPOINTMENT_ID, appointmentId);
        s.setArguments(args);
        return s;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof Listener) {
            listener = (Listener) getParentFragment();
        } else if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            appointmentId = getArguments().getString(ARG_APPOINTMENT_ID);
        }
        if (appointmentId == null) {
            dismiss();
            return;
        }

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnConfirmar.setOnClickListener(v -> changeStatus(AppointmentStatus.CONFIRMED));
        binding.btnCompletar.setOnClickListener(v -> changeStatus(AppointmentStatus.COMPLETED));
        binding.btnCancel.setOnClickListener(v -> confirmCancel());

        loadAppointment();
    }

    private void loadAppointment() {
        setLoading(true);
        db.collection("appointments").document(appointmentId)
                .get()
                .addOnSuccessListener(this::bindAppointment)
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    dismiss();
                });
    }

    private void bindAppointment(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(requireContext(), "Cita no encontrada", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        Appointment appointment = doc.toObject(Appointment.class);
        if (appointment == null) {
            dismiss();
            return;
        }

        DocumentReference clientRef = doc.getDocumentReference("client");
        List<Task<Void>> tasks = new ArrayList<>();

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

        Tasks.whenAll(tasks).addOnSuccessListener(aVoid -> {
            binding.tvServiceName.setText(
                    appointment.getServiceObject() != null ? appointment.getServiceObject().getName() : "Servicio"
            );
            binding.tvProviderName.setText(
                    appointment.getProviderObject() != null ? appointment.getProviderObject().getName() : "Proveedor"
            );

            // fechas
            Timestamp startTs = appointment.getStartAt();
            Timestamp endTs = appointment.getEndAt();
            if (startTs != null) {
                ZonedDateTime startZ = startTs.toDate().toInstant()
                        .atZone(ZoneId.systemDefault());
                String text = dateTimeFmt.format(startZ);
                if (endTs != null) {
                    ZonedDateTime endZ = endTs.toDate().toInstant()
                            .atZone(ZoneId.systemDefault());
                    text += " - " + endZ.toLocalTime().toString();
                }
                binding.tvDateTime.setText(text);
            }

            if (appointment.getPayment() != null) {
                binding.tvPrice.setText(
                        String.format(Locale.getDefault(),
                                "Total: $%.2f", (appointment.getPayment().getAmountCents() / 100f))
                );
            } else {
                binding.tvPrice.setText("");
            }

            if (appointment.getNotesClient() != null && !appointment.getNotesClient().isEmpty()) {
                binding.tvNotes.setText(appointment.getNotesClient());
                binding.tvNotes.setVisibility(View.VISIBLE);
            } else {
                binding.tvNotes.setVisibility(View.GONE);
            }

            // estado

            boolean canConfirm = appointment.getStatusEnum() == AppointmentStatus.REQUESTED;
            boolean canComplete = appointment.getStatusEnum() == AppointmentStatus.CONFIRMED;
            boolean canCancel = appointment.getStatusEnum() == AppointmentStatus.REQUESTED
                    || appointment.getStatusEnum() == AppointmentStatus.CONFIRMED;

            binding.btnConfirmar.setVisibility(canConfirm ? View.VISIBLE : View.GONE);
            binding.btnCompletar.setVisibility(canComplete ? View.VISIBLE : View.GONE);
            binding.btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

            if (clientRef != null) {
                binding.btnViewClient.setVisibility(View.VISIBLE);
                binding.btnViewClient.setOnClickListener(v -> {
                    if (listener != null) listener.onOpenClient(clientRef.getId());
                    dismiss();
                });
            } else {
                binding.btnViewClient.setVisibility(View.GONE);
            }

            AppointmentStatus status = appointment.getStatusEnum();
            String statusText = status.getLabel().charAt(0) + status.getLabel().substring(1).toLowerCase();

            binding.tvStatus.setText("Estado: " + statusText);

            setLoading(false);
        });
    }

    private void confirmCancel() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar cita")
                .setMessage("¿Seguro que deseas cancelar esta cita?")
                .setPositiveButton("Sí, cancelar", (d, w) -> performCancel())
                .setNegativeButton("No", null)
                .show();
    }

    private void performCancel() {
        setLoading(true);
        changeStatus(AppointmentStatus.CANCELLED);
    }

    private void changeStatus(AppointmentStatus newStatus) {
        setLoading(true);
        repo.updateAppointmentStatus(appointmentId, newStatus, aVoid -> {
            setLoading(false);
            Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onAppointmentUpdated();
            dismiss();
        }, e -> {
            setLoading(false);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.container.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.btnCancel.setEnabled(!loading);
        binding.btnClose.setEnabled(!loading);
        // etc.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
