package com.project.studyroom.service;

import com.project.studyroom.dto.RoomStatusDTO;
import com.project.studyroom.model.Reservation;
import com.project.studyroom.model.Room;
import com.project.studyroom.repository.ReservationRepository;
import com.project.studyroom.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public String createRoom(Room room) throws Exception {
        return roomRepository.save(room);
    }

    public void deleteRoom(String roomId) throws Exception {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Sala não encontrada.");
        }
        Date now = new Date();
        for (Reservation r : reservationRepository.findByRoom(roomId)) {
            if (blocksReservationSlot(r)
                    && r.getEndTime() != null
                    && r.getEndTime().after(now)) {
                throw new IllegalStateException(
                        "Não é possível apagar: existem reservas futuras ativas para esta sala.");
            }
        }
        roomRepository.deleteById(roomId);
    }

    public List<Room> getAllRooms() throws Exception {
        return roomRepository.findAll();
    }

    /** Salas sem reserva confirmada que sobreponha o intervalo (uma reserva bloqueia o slot inteiro). */
    public List<Room> getAvailableRooms(Date start, Date end) throws Exception {
        if (start.after(end)) {
            throw new IllegalArgumentException("A data de início deve ser antes da data de fim.");
        }

        List<Room> allRooms = roomRepository.findAll();
        List<Room> availableRooms = new ArrayList<>();

        for (Room room : allRooms) {
            List<Reservation> conflitos = reservationRepository.findOverlapping(room.getId(), start, end);
            if (conflitos.isEmpty()) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }

    private static boolean blocksReservationSlot(Reservation r) {
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

    private static String toIsoUtc(Date d) {
        return d == null ? null : Instant.ofEpochMilli(d.getTime()).toString();
    }

    public List<RoomStatusDTO> getCurrentRoomStatus() throws Exception {
        List<Room> allRooms = roomRepository.findAll();
        Date now = new Date();
        List<RoomStatusDTO> statusList = new ArrayList<>();

        for (Room room : allRooms) {
            List<Reservation> active = reservationRepository.findActiveAt(room.getId(), now);

            RoomStatusDTO dto = new RoomStatusDTO();
            dto.setRoomId(room.getId());
            dto.setRoomName(room.getName());

            if (!active.isEmpty()) {
                Reservation a = active.get(0);
                dto.setOccupied(true);
                dto.setCurrentOccupant(a.getUserName());
                dto.setUntil(toIsoUtc(a.getEndTime()));
                dto.setNextReservationStart(null);
                dto.setNextReservationEnd(null);
            } else {
                dto.setOccupied(false);
                dto.setCurrentOccupant(null);
                dto.setUntil(null);
                Reservation next = reservationRepository.findNextBlockingAfter(room.getId(), now);
                if (next != null) {
                    dto.setNextReservationStart(toIsoUtc(next.getStartTime()));
                    dto.setNextReservationEnd(toIsoUtc(next.getEndTime()));
                }
            }
            statusList.add(dto);
        }
        return statusList;
    }
}
