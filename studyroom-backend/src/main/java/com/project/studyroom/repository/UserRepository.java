package com.project.studyroom.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.project.studyroom.model.User;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class UserRepository {

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void save(User user) throws ExecutionException, InterruptedException {
        firestore.collection("users").document(user.getId()).set(user).get();
    }

    public User getById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("users").document(id).get().get();
        return document.exists() ?
                document.toObject(User.class) : null;
    }

}
