package com.campus.exception;

/**
 * Exception thrown when attempting to delete a room that contains sensors.
 */
public class RoomNotEmptyException extends Exception {
    private String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Cannot delete room " + roomId + " as it contains active sensors");
        this.roomId = roomId;
    }

    public RoomNotEmptyException(String message, String roomId) {
        super(message);
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
