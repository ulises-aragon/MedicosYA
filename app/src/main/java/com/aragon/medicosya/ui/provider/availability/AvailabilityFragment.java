package com.aragon.medicosya.ui.provider.availability;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aragon.medicosya.databinding.FragmentProviderAvailabilityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AvailabilityFragment extends Fragment {

    private FragmentProviderAvailabilityBinding binding;
    private FirebaseFirestore db;
    private String providerId;

    // arrays para almacenar temporalmente HH:mm por día (1..7)
    private final String[] fromTimes = new String[8];
    private final String[] toTimes = new String[8];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderAvailabilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        providerId = FirebaseAuth.getInstance().getUid();

        if (providerId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }

        // listeners para editar horarios
        setupDayEditor(1, binding.tvMonTime, binding.switchMon, binding.btnMonEdit);
        setupDayEditor(2, binding.tvTueTime, binding.switchTue, binding.btnTueEdit);
        setupDayEditor(3, binding.tvWedTime, binding.switchWed, binding.btnWedEdit);
        setupDayEditor(4, binding.tvThuTime, binding.switchThu, binding.btnThuEdit);
        setupDayEditor(5, binding.tvFriTime, binding.switchFri, binding.btnFriEdit);
        setupDayEditor(6, binding.tvSatTime, binding.switchSat, binding.btnSatEdit);
        setupDayEditor(7, binding.tvSunTime, binding.switchSun, binding.btnSunEdit);

        binding.btnSave.setOnClickListener(v -> saveAll());

        loadAvailability();
    }

    private void setupDayEditor(int weekday, TextView tvTime, Switch sw, View btnEdit) {
        btnEdit.setOnClickListener(v -> openTimeRangePicker(weekday, tvTime, sw));
    }

    private void openTimeRangePicker(int weekday, TextView tvTime, Switch sw) {
        Calendar now = Calendar.getInstance();

        // primer picker: inicio
        TimePickerDialog startPicker = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String from = String.format("%02d:%02d", hourOfDay, minute);
                    // segundo picker: fin
                    TimePickerDialog endPicker = new TimePickerDialog(requireContext(),
                            (view2, hourOfDay2, minute2) -> {
                                String to = String.format("%02d:%02d", hourOfDay2, minute2);
                                fromTimes[weekday] = from;
                                toTimes[weekday] = to;
                                tvTime.setText(from + " - " + to);
                                sw.setChecked(true);
                            },
                            hourOfDay + 1, minute, true);
                    endPicker.show();
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true);

        startPicker.show();
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
    }

    private void loadAvailability() {
        setLoading(true);
        db.collection("providers").document(providerId)
                .collection("availability")
                .whereEqualTo("type", "RECURRING")
                .get()
                .addOnSuccessListener(qs -> {
                    setLoading(false);
                    for (var doc : qs.getDocuments()) {
                        Long wdL = doc.getLong("weekday");
                        if (wdL == null) continue;
                        int wd = wdL.intValue();
                        String from = doc.getString("from");
                        String to = doc.getString("to");
                        Boolean isAvailable = doc.getBoolean("isAvailable");

                        fromTimes[wd] = from;
                        toTimes[wd] = to;

                        String label = (from != null && to != null)
                                ? from + " - " + to
                                : "Sin horario";

                        switch (wd) {
                            case 1:
                                binding.tvMonTime.setText(label);
                                binding.switchMon.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                            case 2:
                                binding.tvTueTime.setText(label);
                                binding.switchTue.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                            case 3:
                                binding.tvWedTime.setText(label);
                                binding.switchWed.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                            case 4:
                                binding.tvThuTime.setText(label);
                                binding.switchThu.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                            case 5:
                                binding.tvFriTime.setText(label);
                                binding.switchFri.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                            case 6:
                                binding.tvSatTime.setText(label);
                                binding.switchSat.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                            case 7:
                                binding.tvSunTime.setText(label);
                                binding.switchSun.setChecked(Boolean.TRUE.equals(isAvailable));
                                break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveAll() {
        setLoading(true);
        String basePath = "providers/" + providerId + "/availability";

        // por simplicidad, guardamos 7 docs: weekday_1..weekday_7
        for (int wd = 1; wd <= 7; wd++) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "RECURRING");
            data.put("weekday", wd);

            boolean enabled;
            switch (wd) {
                case 1: enabled = binding.switchMon.isChecked(); break;
                case 2: enabled = binding.switchTue.isChecked(); break;
                case 3: enabled = binding.switchWed.isChecked(); break;
                case 4: enabled = binding.switchThu.isChecked(); break;
                case 5: enabled = binding.switchFri.isChecked(); break;
                case 6: enabled = binding.switchSat.isChecked(); break;
                case 7: enabled = binding.switchSun.isChecked(); break;
                default: enabled = false;
            }

            data.put("isAvailable", enabled);

            String from = fromTimes[wd];
            String to = toTimes[wd];
            data.put("from", from);
            data.put("to", to);

            db.document(basePath + "/weekday_" + wd)
                    .set(data, SetOptions.merge());
        }

        setLoading(false);
        Toast.makeText(requireContext(), "Disponibilidad guardada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
