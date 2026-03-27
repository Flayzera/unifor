package com.project.studyroom.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.project.studyroom.model.Reservation;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReservationRepository {

    private final Firestore firestore;

    public ReservationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * {@code toObject} nem sempre preenche {@code Date} a partir de {@link Timestamp} do Firestore;
     * sem isso, {@link #findOverlapping} ignora reservas existentes e permite choque de horário.
     */
    private static Date readDateField(DocumentSnapshot doc, String field) {
        if (!doc.contains(field)) {
            return null;
        }
        try {
            Timestamp ts = doc.getTimestamp(field);
            if (ts != null) {
                return ts.toDate();
            }
        } catch (RuntimeException ignored) {
            // Field exists but is not stored as Firestore Timestamp (e.g. legacy Date)
        }
        Object v = doc.get(field);
        if (v == null) {
            return null;
        }
        if (v instanceof Timestamp) {
            return ((Timestamp) v).toDate();
        }
        if (v instanceof Date) {
            return (Date) v;
        }
        try {
            Method m = v.getClass().getMethod("toDate");
            if (Date.class.isAssignableFrom(m.getReturnType())) {
                Object d = m.invoke(v);
                if (d instanceof Date) {
                    return (Date) d;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private void applyDateFieldsFromDocument(DocumentSnapshot doc, Reservation r) {
        if (r == null) {
            return;
        }
        Date st = readDateField(doc, "startTime");
        Date et = readDateField(doc, "endTime");
        if (st != null) {
            r.setStartTime(st);
        }
        if (et != null) {
            r.setEndTime(et);
        }
    }

    /** Status explícito no documento (reforço se o POJO vier incompleto). */
    private void applyStatusFromDocument(DocumentSnapshot doc, Reservation r) {
        if (r == null) {
            return;
        }
        String s = doc.getString("status");
        if (s != null && !s.isEmpty()) {
            r.setStatus(s);
        }
    }

    /**
     * Reserva que ainda bloqueia o slot: CONFIRMED ou legado sem status.
     * CANCELLED / COMPLETED não bloqueiam.
     */
    private static boolean blocksInterval(Reservation r) {
        if (r == null) {
            return false;
        }
        String s = r.getStatus();
        if (s == null || s.isEmpty()) {
            return true;
        }
        String u = s.trim().toUpperCase();
        return !"CANCELLED".equals(u) && !"COMPLETED".equals(u);
    }

    /** Firestore pode guardar número como Long; reforça leitura de {@code partySize}. */
    private void applyPartySizeFromDocument(DocumentSnapshot doc, Reservation r) {
        if (r == null || !doc.contains("partySize")) {
            return;
        }
        Object raw = doc.get("partySize");
        if (raw instanceof Number) {
            int v = ((Number) raw).intValue();
            if (v > 0) {
                r.setPartySize(v);
            }
        }
    }

    private Reservation mapReservation(DocumentSnapshot doc) {
        Reservation r = doc.toObject(Reservation.class);
        if (r == null) {
            return null;
        }
        r.setId(doc.getId());
        applyDateFieldsFromDocument(doc, r);
        applyStatusFromDocument(doc, r);
        applyPartySizeFromDocument(doc, r);
        return r;
    }

    //cria reserva
    public String save(Reservation reservation) throws Exception{
        ApiFuture<DocumentReference> future = firestore.collection("reservations").add(reservation);
        return future.get().getId();
    }

    //lista todas as reservas
    public List<Reservation> findAll() throws Exception{
        ApiFuture<QuerySnapshot> future = firestore.collection("reservations").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        return documents.stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());
    }

    //lista reservas por user
    public List<Reservation> findByUserId(String userId) throws Exception{
        ApiFuture<QuerySnapshot> future = firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .get();

        return future.get().getDocuments().stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());
    }

    //lista reservas por sala
    public List<Reservation> findByRoom(String roomId) throws Exception{
        ApiFuture<QuerySnapshot> future = firestore.collection("reservations")
                .whereEqualTo("roomId", roomId)
                .get();

        return future.get().getDocuments().stream()
                .map(this::mapReservation)
                .collect(Collectors.toList());
    }

    /**
     * Sobreposição com [start, end): {@code res.start < end && res.end > start}.
     * Apenas {@code roomId} no Firestore (índice automático); status, intervalo e sobreposição em memória.
     * Assim não é necessário índice composto (ex.: roomId + status).
     */
    public List<Reservation> findOverlapping(String roomId, Date start, Date end) throws Exception {
        ApiFuture<QuerySnapshot> future = firestore.collection("reservations")
                .whereEqualTo("roomId", roomId)
                .get();
        return future.get().getDocuments().stream()
                .map(this::mapReservation)
                .filter(r -> r != null
                        && blocksInterval(r)
                        && r.getStartTime() != null
                        && r.getEndTime() != null
                        && r.getStartTime().before(end)
                        && r.getEndTime().after(start))
                .collect(Collectors.toList());
    }

    /**
     * Sala ocupada neste instante: {@code start ≤ now < end} (fim exclusivo), apenas reservas que ainda bloqueiam.
     */
    public List<Reservation> findActiveAt(String roomId, Date now) throws Exception {
        ApiFuture<QuerySnapshot> future = firestore.collection("reservations")
                .whereEqualTo("roomId", roomId)
                .get();
        return future.get().getDocuments().stream()
                .map(this::mapReservation)
                .filter(r -> r != null
                        && blocksInterval(r)
                        && r.getStartTime() != null
                        && r.getEndTime() != null
                        && !r.getStartTime().after(now)
                        && r.getEndTime().after(now))
                .collect(Collectors.toList());
    }

    /** Próxima reserva que bloqueia o slot, com início estritamente depois de {@code now}. */
    public Reservation findNextBlockingAfter(String roomId, Date now) throws Exception {
        return findByRoom(roomId).stream()
                .filter(r -> r != null
                        && blocksInterval(r)
                        && r.getStartTime() != null
                        && r.getStartTime().after(now))
                .min(Comparator.comparing(Reservation::getStartTime))
                .orElse(null);
    }

    //atualiza status
    public void updateStatus(String reservationId, String newStatus) throws Exception{
        firestore.collection("reservations").document(reservationId)
                .update("status", newStatus).get();
    }

    //encontrar reserva pelo id
    public Reservation findById(String reservationId) throws Exception{
        DocumentSnapshot doc = firestore.collection("reservations").document(reservationId).get().get();
        if (!doc.exists()) {
            return null;
        }
        return mapReservation(doc);
    }

    public void updateTime(String reservationId, Date newStart, Date newEnd) throws Exception{
        firestore.collection("reservations").document(reservationId)
                .update("startTime", newStart,  "endTime", newEnd).get();
    }

    /**
     * Reservas totalmente dentro de [start, end]: {@code startTime >= start && endTime <= end}.
     * Uma única desigualdade no Firestore + filtro em memória (evita índice composto).
     */
    public List<Reservation> findByTimeFrame(Date start, Date end) throws Exception {
        return firestore.collection("reservations")
                .whereGreaterThanOrEqualTo("startTime", start)
                .get().get().getDocuments().stream()
                .map(this::mapReservation)
                .filter(r -> r != null
                        && r.getEndTime() != null
                        && !r.getEndTime().after(end))
                .collect(Collectors.toList());
    }
}
