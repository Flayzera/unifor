package com.project.studyroom.dto;

public class RoomStatusDTO {

    private String roomId;
    private String roomName;
    private boolean isOccupied;
    private String currentOccupant;
    private String until;
    /** Início da próxima reserva que bloqueia (ISO-8601), quando a sala está livre agora. */
    private String nextReservationStart;
    private String nextReservationEnd;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getCurrentOccupant() {
        return currentOccupant;
    }

    public void setCurrentOccupant(String currentOccupant) {
        this.currentOccupant = currentOccupant;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public String getNextReservationStart() {
        return nextReservationStart;
    }

    public void setNextReservationStart(String nextReservationStart) {
        this.nextReservationStart = nextReservationStart;
    }

    public String getNextReservationEnd() {
        return nextReservationEnd;
    }

    public void setNextReservationEnd(String nextReservationEnd) {
        this.nextReservationEnd = nextReservationEnd;
    }
}
