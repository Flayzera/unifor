package com.project.studyroom.service;

import com.project.studyroom.model.Reservation;
import com.project.studyroom.model.Room;
import com.project.studyroom.repository.ReservationRepository;
import com.project.studyroom.repository.RoomRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    public String save(Reservation reservation) throws Exception {

        if (reservation.getStartTime().after(reservation.getEndTime())) {
            throw new IllegalArgumentException("O horário de início deve ser anterior ao horário de fim.");
        }

        Room room = roomRepository.findById(reservation.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("Sala não encontrada.");
        }

        int people = reservation.getPartySize() != null && reservation.getPartySize() > 0
                ? reservation.getPartySize()
                : 1;
        people = Math.min(Math.max(people, 1), room.getCapacity());
        reservation.setPartySize(people);

        if (reservation.getRoomName() == null) {
            reservation.setRoomName(room.getName());
        }

        List<Reservation> conflitos = reservationRepository.findOverlapping(
                reservation.getRoomId(), reservation.getStartTime(), reservation.getEndTime());
        if (!conflitos.isEmpty()) {
            throw new IllegalStateException("A sala selecionada não está disponível neste horário.");
        }

        reservation.setStatus("CONFIRMED");
        reservation.setCreatedAt(new Date());

        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAll() throws Exception {
        return reservationRepository.findAll();
    }

    public List<Reservation> findByUser(String userId) throws Exception {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> findByTimeFrame(Date start, Date end) throws Exception {
        return reservationRepository.findByTimeFrame(start, end);
    }

    public List<Reservation> findByRoom(String roomId) throws Exception {
        return reservationRepository.findByRoom(roomId);
    }

    public List<Reservation> findOverlapping(String roomId, Date start, Date end) throws Exception {
        return reservationRepository.findOverlapping(roomId, start, end);
    }

    public void reschedule(String reservationId, Date newStart, Date newEnd, String loggedUserId) throws Exception {
        if (newStart.after(newEnd)) {
            throw new IllegalArgumentException("A data de início deve ser antes da date de fim.");
        }

        Reservation existing = reservationRepository.findById(reservationId);

        if (existing == null) {
            throw new IllegalArgumentException("Reserva não encontrada");
        }

        if (!existing.getUserId().equals(loggedUserId) && !isAdmin()) {
            throw new IllegalAccessException("Você não tem permissão para modificar esta reserva.");
        }

        List<Reservation> conflitos = reservationRepository.findOverlapping(existing.getRoomId(), newStart, newEnd);
        conflitos.removeIf(r -> r.getId() != null && r.getId().equals(reservationId));

        if (!conflitos.isEmpty()) {
            throw new IllegalStateException("A sala selecionada não está disponível neste horário");
        }

        reservationRepository.updateTime(reservationId, newStart, newEnd);
    }

    public void updateStatus(String reservationId, String newStatus, String loggedUserId) throws Exception {

        List<String> statuses = List.of("CONFIRMED", "CANCELLED", "COMPLETED");

        if (reservationId == null || reservationId.isEmpty()) {
            throw new IllegalArgumentException("O ID da reserva é obrigatório");
        }

        if (!statuses.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Status inválido. Use CONFIRMED, CANCELLED, COMPLETED");
        }

        Reservation existing = reservationRepository.findById(reservationId);
        if (!existing.getUserId().equals(loggedUserId) && !isAdmin()) {
            throw new IllegalAccessException("Você não tem permissão para alterar o status desta reserva.");
        }

        reservationRepository.updateStatus(reservationId, newStatus.toUpperCase());
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    }

}
