package com.campus.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import com.campus.model.Sensor;
import com.campus.exception.LinkedResourceNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sensor Resource for CRUD operations on Sensor entities.
 * Endpoints: /api/v1/sensors
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private static final Logger LOGGER = Logger.getLogger(SensorResource.class.getName());
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final AtomicInteger sensorCounter = new AtomicInteger(1);
    
    // Initialize with sample data
    static {
        sensors.put("T001", new Sensor("T001", "L1", "TEMPERATURE", "ACTIVE", "Window side"));
        sensors.put("H001", new Sensor("H001", "L1", "HUMIDITY", "ACTIVE", "Center"));
        sensors.put("T002", new Sensor("T002", "S101", "TEMPERATURE", "MAINTENANCE", "Door"));
        sensors.put("M001", new Sensor("M001", "L201", "MOTION", "ACTIVE", "Entrance"));
    }

    /**
     * GET all sensors with optional filtering by type
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        LOGGER.log(Level.INFO, "Fetching all sensors" + (type != null ? " of type: " + type : ""));
        
        Collection<Sensor> result;
        if (type != null && !type.isEmpty()) {
            result = sensors.values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .toList();
        } else {
            result = sensors.values();
        }
        
        return Response.ok(result).build();
    }

    /**
     * GET a specific sensor by ID
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        LOGGER.log(Level.INFO, "Fetching sensor: " + sensorId);
        
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        return Response.ok(sensor).build();
    }

    /**
     * POST create a new sensor
     * Business Rule: Room must exist
     */
    @POST
    public Response createSensor(Sensor sensor) throws LinkedResourceNotFoundException {
        LOGGER.log(Level.INFO, "Creating new sensor");

        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Request body is required and must be valid JSON\"}")
                    .build();
        }
        
        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Room ID is required\"}")
                    .build();
        }
        
        // Verify room exists
        if (RoomResource.lookupRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }
        
        // Generate sensor ID if not provided
        if (sensor.getSensorId() == null || sensor.getSensorId().isEmpty()) {
            String type = sensor.getType() != null ? sensor.getType().substring(0, 1) : "S";
            sensor.setSensorId(type + String.format("%03d", sensorCounter.getAndIncrement()));
        }
        
        if (sensors.containsKey(sensor.getSensorId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Sensor already exists\", \"sensorId\": \"" + sensor.getSensorId() + "\"}")
                    .build();
        }
        
        // Set default status
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }
        
        sensors.put(sensor.getSensorId(), sensor);
        URI location = UriBuilder.fromPath("/api/v1/sensors/{sensorId}")
            .build(sensor.getSensorId());
        return Response.created(location).entity(sensor).build();
    }

    /**
     * PUT update an existing sensor
     */
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        LOGGER.log(Level.INFO, "Updating sensor: " + sensorId);

        if (updatedSensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Request body is required and must be valid JSON\"}")
                    .build();
        }
        
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        updatedSensor.setSensorId(sensorId);
        sensors.put(sensorId, updatedSensor);
        return Response.ok(updatedSensor).build();
    }

    /**
     * DELETE a sensor by ID
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        LOGGER.log(Level.INFO, "Deleting sensor: " + sensorId);
        
        if (!sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        // Also delete associated readings
        SensorReadingResource.deleteSensorReadings(sensorId);
        
        Sensor deletedSensor = sensors.remove(sensorId);
        return Response.ok(deletedSensor).build();
    }

    // Static methods for other resources to access sensors
    public static Sensor lookupSensor(String sensorId) {
        return sensors.get(sensorId);
    }

    public static long getRoomSensorCount(String roomId) {
        return sensors.values().stream()
                .filter(s -> s.getRoomId().equals(roomId))
                .count();
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }
}
