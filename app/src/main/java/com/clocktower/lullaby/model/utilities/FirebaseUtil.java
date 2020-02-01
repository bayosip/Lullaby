package com.clocktower.lullaby.model.utilities;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {

    static FirebaseFirestore firestore;
    static StorageReference storage;

    static {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
    }


    public static FirebaseFirestore getFirestore() {
        return firestore;
    }

    public static StorageReference getStorage() {
        return storage;
    }
}
