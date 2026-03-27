package com.project.studyroom.controller;

import com.project.studyroom.dto.RoomStatusDTO;
import com.project.studyroom.model.Room;
import com.project.studyroom.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@RequestBody Room room) throws Exception {
            String id = roomService.createRoom(room);
            return ResponseEntity.ok("Sala criada com id " + id);
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailable(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Date start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Date end
    ) throws Exception {
        List<Room> rooms = roomService.getAvailableRooms(start, end);
        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable String id) throws Exception {
        roomService.deleteRoom(id);
        return ResponseEntity.ok("Sala removida.");
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAll() throws Exception {
            return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomStatusDTO>> getAdminDashboard() throws Exception {
        return ResponseEntity.ok(roomService.getCurrentRoomStatus());
    }
}
