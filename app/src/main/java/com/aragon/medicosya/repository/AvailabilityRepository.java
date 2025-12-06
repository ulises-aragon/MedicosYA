package com.aragon.medicosya.repository;

import com.aragon.medicosya.models.Availability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AvailabilityRepository {
    private final FirebaseFirestore firestore;

    public AvailabilityRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    private CollectionReference availRef(String providerId) {
        return firestore.collection("providers").document(providerId).collection("availability");
    }

    public void addAvailability(String providerId, Availability availability, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", availability.getType() == null ? null : availability.getType().toString());
        map.put("weekday", availability.getWeekday());
        map.put("from", availability.getFrom());
        map.put("to", availability.getTo());
        map.put("date", availability.getDate());
        map.put("isAvailable", availability.getIsAvailable());
        map.put("createdAt", Timestamp.now());

        availRef(providerId).add(map)
                .addOnSuccessListener(docRef -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void updateAvailability(String providerId, String availabilityId, Availability availability, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", availability.getType() == null ? null : availability.getType().toString());
        map.put("weekday", availability.getWeekday());
        map.put("from", availability.getFrom());
        map.put("to", availability.getTo());
        map.put("date", availability.getDate());
        map.put("isAvailable", availability.getIsAvailable());
        availRef(providerId).document(availabilityId).set(map)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void getAvailabilityForDate(String providerId, String dateStr, int weekday,
                                       OnSuccessListener<List<Availability>> onSuccess, OnFailureListener onFailure) {
        availRef(providerId).whereEqualTo("type", "RECURRING").whereEqualTo("weekday", weekday).get()
                .addOnSuccessListener(recSnap -> {
                    List<Availability> result = new ArrayList<>();
                    for (DocumentSnapshot ds : recSnap.getDocuments()) {
                        Availability av = ds.toObject(Availability.class);
                        if (av != null) {
                            av.setId(ds.getId());
                            result.add(av);
                        }
                    }
                    availRef(providerId).whereEqualTo("type", "EXCEPTION").whereEqualTo("date", dateStr).get()
                            .addOnSuccessListener(excSnap -> {
                                for (DocumentSnapshot ds : excSnap.getDocuments()) {
                                    Availability av = ds.toObject(Availability.class);
                                    if (av != null) {
                                        av.setId(ds.getId());
                                        result.add(av);
                                    }
                                }
                                onSuccess.onSuccess(result);
                            }).addOnFailureListener(onFailure);
                }).addOnFailureListener(onFailure);
    }

    public void setRecurringAvailability(DocumentReference providerRef,
                                         int weekday,
                                         String from,
                                         String to,
                                         boolean isAvailable,
                                         OnSuccessListener<Void> onSuccess,
                                         OnFailureListener onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "RECURRING");
        data.put("weekday", weekday);
        data.put("from", from);
        data.put("to", to);
        data.put("isAvailable", isAvailable);

        // podrías usar documentId = weekday-from-to o auto-id
        providerRef.collection("availability")
                .add(data)
                .addOnSuccessListener(docRef -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void getAvailabilityForProvider(DocumentReference providerRef,
                                           OnSuccessListener<List<Availability>> onSuccess,
                                           OnFailureListener onFailure) {
        providerRef.collection("availability")
                .get()
                .addOnSuccessListener(qs -> {
                    List<Availability> list = qs.toObjects(Availability.class);
                    for (int i = 0; i < qs.size(); i++) {
                        list.get(i).setId(qs.getDocuments().get(i).getId());
                    }
                    onSuccess.onSuccess(list);
                })
                .addOnFailureListener(onFailure);
    }
}
