package com.project.studyroom.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.project.studyroom.model.Room;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RoomRepository {

    private final Firestore firestore;

    public RoomRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    //adiciona sala
    public String save(Room room) throws Exception{
        ApiFuture<DocumentReference> future = firestore.collection("rooms").add(room);
        return future.get().getId();
    }

    //lista todas as salas
    public List<Room> findAll() throws Exception{
        ApiFuture<QuerySnapshot> future = firestore.collection("rooms").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        return documents.stream()
                .map(doc -> {
                    Room r = doc.toObject(Room.class);
                    r.setId(doc.getId());
                    return r;
                })
                .collect(Collectors.toList());
    }

    public Room findById(String id) throws Exception {
        DocumentSnapshot doc = firestore.collection("rooms").document(id).get().get();
        if (!doc.exists()) {
            return null;
        }
        Room r = doc.toObject(Room.class);
        if (r != null) {
            r.setId(doc.getId());
        }
        return r;
    }

    public void deleteById(String id) throws Exception {
        firestore.collection("rooms").document(id).delete().get();
    }

}
