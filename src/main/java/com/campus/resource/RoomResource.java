package com.campus.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import com.campus.model.Room;
import com.campus.exception.RoomNotEmptyException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Room Resource for CRUD operations on Room entities.
 * Endpoints: /api/v1/rooms
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private static final Logger LOGGER = Logger.getLogger(RoomResource.class.getName());
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    // Initialize with sample data
    static {
        rooms.put("L1", new Room("L1", "Lecture Hall 1", "Engineering", 1, 150));
        rooms.put("S101", new Room("S101", "Seminar 101", "Science", 1, 30));
        rooms.put("L201", new Room("L201", "Lab 201", "Engineering", 2, 50));
    }

    /**
     * GET all rooms or filter by building
     */
    @GET
    public Response getAllRooms(@QueryParam("building") String building) {
        LOGGER.log(Level.INFO, "Fetching all rooms" + (building != null ? " in building: " + building : ""));
        
        Collection<Room> result;
        if (building != null && !building.isEmpty()) {
            result = rooms.values().stream()
                    .filter(r -> r.getBuilding().equalsIgnoreCase(building))
                    .toList();
        } else {
            result = rooms.values();
        }
        
        return Response.ok(result).build();
    }

    /**
     * GET a specific room by ID
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        LOGGER.log(Level.INFO, "Fetching room: " + roomId);
        
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\", \"roomId\": \"" + roomId + "\"}")
                    .build();
        }
        
        return Response.ok(room).build();
    }

    /**
     * POST create a new room
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Request body is required and must be valid JSON\"}")
                    .build();
        }

        LOGGER.log(Level.INFO, "Creating new room: " + room.getRoomId());
        
        if (room.getRoomId() == null || room.getRoomId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required\"}")
                    .build();
        }
        
        if (rooms.containsKey(room.getRoomId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Room already exists\", \"roomId\": \"" + room.getRoomId() + "\"}")
                    .build();
        }
        
        rooms.put(room.getRoomId(), room);
        URI location = UriBuilder.fromPath("/api/v1/rooms/{roomId}")
            .build(room.getRoomId());
        return Response.created(location).entity(room).build();
    }

    /**
     * PUT update an existing room
     */
    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        LOGGER.log(Level.INFO, "Updating room: " + roomId);

        if (updatedRoom == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Request body is required and must be valid JSON\"}")
                    .build();
        }
        
        if (!rooms.containsKey(roomId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\", \"roomId\": \"" + roomId + "\"}")
                    .build();
        }
        
        updatedRoom.setRoomId(roomId);
        rooms.put(roomId, updatedRoom);
        return Response.ok(updatedRoom).build();
    }

    /**
     * DELETE a room by ID
     * Business Rule: Cannot delete room with sensors
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) throws RoomNotEmptyException {
        LOGGER.log(Level.INFO, "Deleting room: " + roomId);
        
        if (!rooms.containsKey(roomId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Room not found\", \"roomId\": \"" + roomId + "\"}")
                    .build();
        }
        
        // Check if room has sensors - get sensors from SensorResource
        if (SensorResource.getRoomSensorCount(roomId) > 0) {
            throw new RoomNotEmptyException(roomId);
        }
        
        Room deletedRoom = rooms.remove(roomId);
        return Response.ok(deletedRoom).build();
    }

    // Static method for other resources to access rooms
    public static Room lookupRoom(String roomId) {
        return rooms.get(roomId);
    }

    public static Map<String, Room> getRooms() {
        return rooms;
    }
}
