package com.project.studyroom.service;

import com.project.studyroom.model.Reservation;
import com.project.studyroom.model.Room;
import com.project.studyroom.repository.ReservationRepository;
import com.project.studyroom.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void after() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_rejectsWhenStartAfterEnd() {
        Reservation r = new Reservation();
        r.setRoomId("room1");
        r.setStartTime(new Date(10_000));
        r.setEndTime(new Date(5_000));

        assertThrows(IllegalArgumentException.class, () -> reservationService.save(r));
    }

    @Test
    void save_rejectsWhenRoomNotFound() throws Exception {
        Reservation r = new Reservation();
        r.setRoomId("missing");
        r.setStartTime(new Date(1_000));
        r.setEndTime(new Date(5_000));
        when(roomRepository.findById("missing")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> reservationService.save(r));
    }

    @Test
    void save_clampsPartySizeToRoomCapacity() throws Exception {
        Room room = new Room();
        room.setId("r1");
        room.setName("Sala A");
        room.setCapacity(4);
        when(roomRepository.findById("r1")).thenReturn(room);
        when(reservationRepository.findOverlapping(eq("r1"), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn("new-id");

        Reservation r = new Reservation();
        r.setRoomId("r1");
        r.setStartTime(new Date(1_000));
        r.setEndTime(new Date(10_000));
        r.setPartySize(99);

        reservationService.save(r);

        verify(reservationRepository).save(org.mockito.ArgumentMatchers.argThat(res -> res.getPartySize() == 4));
    }

    @Test
    void save_rejectsWhenSlotOverlaps() throws Exception {
        Room room = new Room();
        room.setId("r1");
        room.setName("Sala A");
        room.setCapacity(10);
        when(roomRepository.findById("r1")).thenReturn(room);

        Reservation conflict = new Reservation();
        conflict.setId("other");
        when(reservationRepository.findOverlapping(eq("r1"), any(), any())).thenReturn(List.of(conflict));

        Reservation r = new Reservation();
        r.setRoomId("r1");
        r.setStartTime(new Date(1_000));
        r.setEndTime(new Date(10_000));

        assertThrows(IllegalStateException.class, () -> reservationService.save(r));
    }

    @Test
    void reschedule_throwsWhenNotOwnerAndNotAdmin() throws Exception {
        Reservation existing = new Reservation();
        existing.setId("res1");
        existing.setUserId("owner");
        existing.setRoomId("room1");
        when(reservationRepository.findById("res1")).thenReturn(existing);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "intruso",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));

        Date start = new Date(1000);
        Date end = new Date(5000);

        assertThrows(IllegalAccessException.class,
                () -> reservationService.reschedule("res1", start, end, "intruso"));
    }

    @Test
    void updateStatus_rejectsInvalidStatus() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "u1",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.updateStatus("res1", "INVALID", "u1"));
    }

    @Test
    void isAdmin_trueWhenRoleAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertEquals(true, reservationService.isAdmin());
    }
}
