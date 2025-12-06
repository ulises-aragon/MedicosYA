package com.aragon.medicosya.ui.provider.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.aragon.medicosya.databinding.ActivityProviderBookingBinding;
import com.aragon.medicosya.enums.AppointmentStatus;
import com.aragon.medicosya.enums.AvailabilityType;
import com.aragon.medicosya.enums.PaymentMethod;
import com.aragon.medicosya.enums.PaymentStatus;
import com.aragon.medicosya.models.Appointment;
import com.aragon.medicosya.models.Availability;
import com.aragon.medicosya.models.Payment;
import com.aragon.medicosya.models.Service;
import com.aragon.medicosya.repository.AppointmentRepository;
import com.aragon.medicosya.repository.ServiceRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity implements SlotAdapter.SlotClick {

    private ActivityProviderBookingBinding binding;
    private final ServiceRepository serviceRepository = new ServiceRepository();
    private final AppointmentRepository appointmentRepository = new AppointmentRepository();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private String providerId;
    private String clientId;
    private DocumentReference providerRef;

    private List<Service> services = new ArrayList<>();
    private Service selectedService;

    private final SlotAdapter slotAdapter = new SlotAdapter(this);

    private LocalDate chosenDate = null;
    private Slot selectedSlot;

    private final int spanCount = 2;
    private final int paddingMinutes = 5;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
    private static final DateTimeFormatter DATE_FORMAT_FRIENDLY = DateTimeFormatter.ofPattern("eeee, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProviderBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        providerId = getIntent().getStringExtra("providerId");
        clientId = getIntent().getStringExtra("clientId");

        if (providerId == null) {
            Toast.makeText(this, "Proveedor inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        providerRef = firestore.collection("providers").document(providerId);

        binding.rvSlots.setLayoutManager(new GridLayoutManager(this, spanCount));
        binding.rvSlots.setAdapter(slotAdapter);

        loadServices();

        binding.spinnerServices.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < services.size()) {
                    selectedService = services.get(position);
                    updateServiceMeta();
                    // if date chosen, recalc slots
                    if (chosenDate != null) generateSlotsForChosenDate();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { selectedService = null; }
        });

        binding.btnPickDate.setOnClickListener(v -> openDatePicker());
        binding.exitBooking.setOnClickListener(v -> finish());
        binding.bttnComplete.setOnClickListener(v -> bookAppointment());

        binding.bttnComplete.setEnabled(false);
    }

    private void loadServices() {
        serviceRepository.getServicesForProvider(providerId, svcList -> {
            services.clear();
            services.addAll(svcList);

            List<String> names = new ArrayList<>();
            for (Service service : services) {
                names.add(service.getName() + " · " + service.getDuration() + "min");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerServices.setAdapter(adapter);
        }, e -> Toast.makeText(this, "Error cargando servicios: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateServiceMeta() {
        if (selectedService == null) {
            binding.tvSelectedServiceMeta.setText("");
            return;
        }
        binding.tvSelectedServiceMeta.setText(String.format(Locale.getDefault(), "%d min · $%.2f",
                selectedService.getDuration(), (float) selectedService.getPriceCents()/100));
    }

    private void openDatePicker() {
        final Calendar today = Calendar.getInstance();
        final Calendar initialdate = Calendar.getInstance();
        if (chosenDate != null) {
            initialdate.setTime(Date.from(chosenDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            chosenDate = LocalDate.of(year, month + 1, dayOfMonth);
            String formattedDate = DATE_FORMAT_FRIENDLY.format(chosenDate);

            if (formattedDate != null && !formattedDate.isEmpty()) {
                String displayDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                binding.tvDateSelected.setText(displayDate);
            } else {
                binding.tvDateSelected.setText(formattedDate);
            }

            generateSlotsForChosenDate();
        },
                initialdate.get(Calendar.YEAR),
                initialdate.get(Calendar.MONTH),
                initialdate.get(Calendar.DAY_OF_MONTH));

        dpd.getDatePicker().setMinDate(today.getTimeInMillis());
        dpd.show();
    }
    private void generateSlotsForChosenDate() {
        if (chosenDate == null) return;
        if (selectedService == null) {
            Toast.makeText(this, "Selecciona un servicio primero", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedSlot = null;
        binding.bttnComplete.setEnabled(false);

        binding.progress.setVisibility(View.VISIBLE);
        binding.tvNoSlots.setVisibility(View.GONE);
        binding.rvSlots.setVisibility(View.GONE);

        final String dateStr = DATE_FORMAT.format(chosenDate);
        final int weekday = chosenDate.getDayOfWeek().getValue() % 7;

        providerRef.collection("availability")
                .get()
                .addOnSuccessListener(query -> {
                    List<Availability> recurring = new ArrayList<>();
                    List<Availability> exceptions = new ArrayList<>();

                    for (DocumentSnapshot documentSnapshot : query.getDocuments()) {
                        Availability availability = documentSnapshot.toObject(Availability.class);
                        if (availability == null) continue;
                        availability.setId(documentSnapshot.getId());
                        if (availability.getType() != null && availability.getTypeEnum() == AvailabilityType.RECURRING) {
                            if (availability.getWeekday() != null && availability.getWeekday() == weekday) recurring.add(availability);
                        } else if (availability.getType() != null && availability.getTypeEnum() == AvailabilityType.EXCEPTION) {
                            if (availability.getDate() != null && availability.getDate().equals(dateStr)) exceptions.add(availability);
                        }
                    }

                    ZoneId zone = ZoneId.systemDefault();
                    List<Interval> intervals = new ArrayList<>();
                    for (Availability r : recurring) {
                        // r.from, r.to are "HH:mm"
                        Interval iv = toIntervalForDateUsingJavaTime(r.getFrom(), r.getTo(), chosenDate, zone);
                        if (iv != null && r.getIsAvailable() != null && r.getIsAvailable()) intervals.add(iv);
                    }

                    for (Availability exc : exceptions) {
                        Interval excIv = toIntervalForDateUsingJavaTime(exc.getFrom(), exc.getTo(), chosenDate, zone);
                        if (excIv == null) continue;
                        if (Boolean.FALSE.equals(exc.getIsAvailable())) {
                            // remove overlapping parts from intervals
                            intervals = subtractIntervalList(intervals, excIv);
                        } else {
                            // add exception interval (possible extra)
                            intervals.add(excIv);
                        }
                    }

                    int durationMin = selectedService.getDuration();
                    int stepMin = durationMin + paddingMinutes;

                    List<Slot> candidateSlots = new ArrayList<>();
                    for (Interval iv : intervals) {
                        ZonedDateTime cursor = iv.start;
                        while (!cursor.plusMinutes(durationMin).isAfter(iv.end)) {
                            ZonedDateTime slotEnd = cursor.plusMinutes(durationMin);
                            String label = TIME_LABEL.format(cursor.toLocalTime()) + " - " + TIME_LABEL.format(slotEnd.toLocalTime());
                            candidateSlots.add(new Slot(cursor.toInstant().toEpochMilli(), slotEnd.toInstant().toEpochMilli(), label));
                            cursor = cursor.plusMinutes(stepMin);
                        }
                    }

                    if (candidateSlots.isEmpty()) {
                        binding.progress.setVisibility(View.GONE);
                        binding.tvNoSlots.setVisibility(View.VISIBLE);
                        binding.rvSlots.setVisibility(View.GONE);
                        return;
                    }

                    ZonedDateTime dayStartZ = chosenDate.atStartOfDay(zone);
                    ZonedDateTime dayEndZ = dayStartZ.plusDays(1);

                    firestore.collection("appointments")
                            .whereEqualTo("provider", providerRef)
                            .whereIn("status", java.util.Arrays.asList(AppointmentStatus.REQUESTED.toString(), AppointmentStatus.CONFIRMED.toString()))
                            .get()
                            .addOnSuccessListener(appSnap -> {
                                List<Interval> booked = new ArrayList<>();
                                for (DocumentSnapshot dsApp : appSnap.getDocuments()) {
                                    Timestamp s = dsApp.getTimestamp("startAt");
                                    Timestamp e = dsApp.getTimestamp("endAt");
                                    if (s == null || e == null) continue;
                                    Instant sInst = s.toDate().toInstant();
                                    Instant eInst = e.toDate().toInstant();

                                    ZonedDateTime sZ = ZonedDateTime.ofInstant(sInst, zone);
                                    ZonedDateTime eZ = ZonedDateTime.ofInstant(eInst, zone);

                                    if (sZ.isBefore(dayEndZ) && eZ.isAfter(dayStartZ)) {
                                        booked.add(new Interval(sZ, eZ));
                                    }
                                }


                                // filter candidateSlots
                                List<Slot> available = new ArrayList<>();
                                for (Slot slot : candidateSlots) {
                                    boolean conflict = false;
                                    long sMillis = slot.startMillis;
                                    long eMillis = slot.endMillis;
                                    for (Interval b : booked) {
                                        if (sMillis < b.end.toInstant().toEpochMilli() && eMillis > b.start.toInstant().toEpochMilli()) {
                                            conflict = true; break;
                                        }
                                    }
                                    if (!conflict) available.add(slot);
                                }

                                // update UI
                                binding.progress.setVisibility(View.GONE);
                                if (available.isEmpty()) {
                                    binding.tvNoSlots.setVisibility(View.VISIBLE);
                                    binding.rvSlots.setVisibility(View.GONE);
                                } else {
                                    binding.tvNoSlots.setVisibility(View.GONE);
                                    binding.rvSlots.setVisibility(View.VISIBLE);
                                    slotAdapter.setSlots(available);
                                }

                            }).addOnFailureListener(e -> {
                                binding.progress.setVisibility(View.GONE);
                                Toast.makeText(this, "Error leyendo citas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                }).addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error leyendo availability: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // convertir "HH:mm" a Interval para chosenDate en zona
    private Interval toIntervalForDateUsingJavaTime(String from, String to, LocalDate date, ZoneId zone) {
        try {
            if (from == null || to == null) return null;
            LocalTime ltFrom = LocalTime.parse(from);
            LocalTime ltTo = LocalTime.parse(to);
            ZonedDateTime start = ZonedDateTime.of(date, ltFrom, zone);
            ZonedDateTime end = ZonedDateTime.of(date, ltTo, zone);
            if (!end.isAfter(start)) return null; // descartar cruces a la madrugada
            return new Interval(start, end);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<Interval> subtractIntervalList(List<Interval> list, Interval exc) {
        List<Interval> out = new ArrayList<>();
        for (Interval iv : list) {
            if (exc.end.isEqual(iv.start) || exc.end.isBefore(iv.start) || exc.start.isAfter(iv.end) || exc.start.isEqual(iv.end)) {
                // no overlap
                out.add(iv);
            } else {
                // overlap: split if needed
                if (exc.start.isAfter(iv.start)) {
                    out.add(new Interval(iv.start, exc.start));
                }
                if (exc.end.isBefore(iv.end)) {
                    out.add(new Interval(exc.end, iv.end));
                }
            }
        }
        return out;
    }

    private void bookAppointment() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedService == null) {
            Toast.makeText(this, "Selecciona un servicio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSlot == null) {
            Toast.makeText(this, "Selecciona un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        long startMillis = selectedSlot.startMillis;
        long endMillis = selectedSlot.endMillis;

        DocumentReference providerRef = firestore.collection("providers").document(providerId);

        Appointment appointment = new Appointment();
        appointment.setClient(firestore.collection("users").document(clientId));
        appointment.setProvider(providerRef);
        appointment.setService(providerRef.collection("services").document(selectedService.getId()));
        appointment.setNotesProvider(binding.editTextNotes.getText().toString());
        appointment.setStatusEnum(AppointmentStatus.REQUESTED);
        appointment.setStartAt(new Timestamp(new Date(startMillis)));
        appointment.setEndAt(new Timestamp(new Date(endMillis)));

        Payment payment = new Payment();
        payment.setMethodEnum(PaymentMethod.CASH);
        payment.setStatusEnum(PaymentStatus.UNPAID);
        payment.setAmountCents(selectedService.getPriceCents());

        appointment.setPayment(payment);

        binding.progress.setVisibility(View.VISIBLE);

        appointmentRepository.createAppointment(appointment, appointmentId -> {
            binding.progress.setVisibility(View.GONE);
            Toast.makeText(this, "Cita solicitada con exito", Toast.LENGTH_LONG).show();
            finish();
        }, e -> {
            binding.progress.setVisibility(View.GONE);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            generateSlotsForChosenDate();
        });
    }

    @Override
    public void onSlotSelected(int position, Slot slot) {
        this.selectedSlot = slot;
        slotAdapter.setSelectedPosition(position);
        binding.bttnComplete.setEnabled(true);
    }

    private static class Interval {
        ZonedDateTime start;
        ZonedDateTime end;
        Interval(ZonedDateTime s, ZonedDateTime e) { this.start = s; this.end = e; }
    }

    public class Slot {
        public long startMillis;
        public long endMillis;
        public String label;
        public Slot() {}
        public Slot(long s, long e, String label) { this.startMillis = s; this.endMillis = e; this.label = label; }
        public String getLabel() { return label; }
    }
}