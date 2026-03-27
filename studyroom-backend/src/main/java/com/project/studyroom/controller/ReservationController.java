package com.project.studyroom.controller;

import com.project.studyroom.config.security.AuthenticatedUser;
import com.project.studyroom.dto.RescheduleRequestDTO;
import com.project.studyroom.model.Reservation;
import com.project.studyroom.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Reservation reservation, @AuthenticatedUser String uid) throws Exception{
            reservation.setUserId(uid);
            String id = reservationService.save(reservation);
            return ResponseEntity.status(201).body("Reserva realizada com sucesso. ID: "+ id);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reservation>> getAll() throws Exception {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getByUserId(@PathVariable("userId") String userId,
                                                           @AuthenticatedUser String uid) throws Exception {
        if (uid == null || (!uid.equals(userId) && !reservationService.isAdmin())) {
            throw new IllegalAccessException("Você não tem permissão para ver as reservas deste usuário.");
        }
        return ResponseEntity.ok(reservationService.findByUser(userId));
    }

    @GetMapping("/room/{roomID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reservation>> getByRoomId(@PathVariable("roomID") String roomID) throws Exception {
        return ResponseEntity.ok(reservationService.findByRoom(roomID));
    }

    @GetMapping("/timeframe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reservation>> getByTimeFrame(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Date start,
                                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Date end) throws Exception {
        return ResponseEntity.ok(reservationService.findByTimeFrame(start, end));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable String id, @AuthenticatedUser String uid) throws Exception {
            reservationService.updateStatus(id, "CANCELLED", uid);
            return ResponseEntity.ok("Reserva cacelada com sucesso.");
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> complete(@PathVariable String id, @AuthenticatedUser String uid) throws Exception {
            reservationService.updateStatus(id, "COMPLETED", uid);
            return ResponseEntity.ok("Reserva completa com sucesso.");
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<String> reschedule(@PathVariable String id,
                                             @Valid @RequestBody RescheduleRequestDTO request,
                                             @AuthenticatedUser String uid) throws Exception
    {
            reservationService.reschedule(id, request.getNewStart(), request.getNewEnd(), uid);
            return ResponseEntity.ok("Reserva modificada com sucesso.");

    }

}
