package com.aragon.medicosya.ui.client.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aragon.medicosya.models.Provider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProvidersViewModel extends ViewModel {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Provider>> providers = new MutableLiveData<>();

    public LiveData<List<Provider>> getProviders() {
        return providers;
    }

    public void loadProviders() {
        firestore.collection("providers")
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    List<Provider> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Provider p = doc.toObject(Provider.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    providers.postValue(list);
                }).addOnFailureListener(e -> {
                    providers.postValue(new ArrayList<>());
                });
    }
}
