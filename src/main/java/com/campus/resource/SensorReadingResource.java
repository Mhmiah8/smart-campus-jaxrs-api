package com.campus.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.campus.model.Sensor;
import com.campus.model.SensorReading;
import com.campus.exception.SensorUnavailableException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sensor Reading Resource for managing sensor readings.
 * Sub-resource endpoint: /api/v1/sensors/{sensorId}/readings
 */
@Path("/sensors/{sensorId}/readings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private static final Logger LOGGER = Logger.getLogger(SensorReadingResource.class.getName());
    private static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();
    private static final AtomicInteger readingCounter = new AtomicInteger(1);
    
    // Initialize with sample data
    static {
        List<SensorReading> t001Readings = new ArrayList<>();
        t001Readings.add(new SensorReading("R001", "T001", 22.5, "°C", "2024-01-15T10:30:00"));
        t001Readings.add(new SensorReading("R002", "T001", 22.7, "°C", "2024-01-15T10:45:00"));
        readings.put("T001", t001Readings);
        
        List<SensorReading> h001Readings = new ArrayList<>();
        h001Readings.add(new SensorReading("R003", "H001", 55.0, "%", "2024-01-15T10:30:00"));
        readings.put("H001", h001Readings);
    }

    /**
     * GET all readings for a specific sensor
     */
    @GET
    public Response getSensorReadings(@PathParam("sensorId") String sensorId) {
        LOGGER.log(Level.INFO, "Fetching readings for sensor: " + sensorId);
        
        // Verify sensor exists
        Sensor sensor = SensorResource.lookupSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        List<SensorReading> sensorReadings = readings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(sensorReadings).build();
    }

    /**
     * GET a specific reading from a sensor
     */
    @GET
    @Path("/{readingId}")
    public Response getReading(@PathParam("sensorId") String sensorId, 
                             @PathParam("readingId") String readingId) {
        LOGGER.log(Level.INFO, "Fetching reading: " + readingId + " for sensor: " + sensorId);
        
        // Verify sensor exists
        Sensor sensor = SensorResource.lookupSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        List<SensorReading> sensorReadings = readings.getOrDefault(sensorId, new ArrayList<>());
        SensorReading reading = sensorReadings.stream()
                .filter(r -> r.getReadingId().equals(readingId))
                .findFirst()
                .orElse(null);
        
        if (reading == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Reading not found\", \"readingId\": \"" + readingId + "\"}")
                    .build();
        }
        
        return Response.ok(reading).build();
    }

    /**
     * POST create a new reading for a sensor
     * Business Rule: Cannot add reading to sensor under maintenance
     */
    @POST
    public Response createReading(@PathParam("sensorId") String sensorId, 
                                 SensorReading reading) throws SensorUnavailableException {
        LOGGER.log(Level.INFO, "Creating new reading for sensor: " + sensorId);

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Request body is required and must be valid JSON\"}")
                    .build();
        }
        
        // Verify sensor exists
        Sensor sensor = SensorResource.lookupSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        // Check if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }
        
        // Check if sensor is inactive
        if ("INACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }
        
        // Set sensor ID and generate reading ID if needed
        reading.setSensorId(sensorId);
        if (reading.getReadingId() == null || reading.getReadingId().isEmpty()) {
            reading.setReadingId("R" + String.format("%03d", readingCounter.getAndIncrement()));
        }
        
        // Set timestamp if not provided
        if (reading.getTimestamp() == null || reading.getTimestamp().isEmpty()) {
            reading.setTimestamp(SensorReading.getCurrentTimeStamp());
        }
        
        // Get or create readings list for this sensor
        List<SensorReading> sensorReadings = readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
        sensorReadings.add(reading);
        
        // Update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());
        
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    /**
     * DELETE a specific reading from a sensor
     */
    @DELETE
    @Path("/{readingId}")
    public Response deleteReading(@PathParam("sensorId") String sensorId, 
                                 @PathParam("readingId") String readingId) {
        LOGGER.log(Level.INFO, "Deleting reading: " + readingId + " from sensor: " + sensorId);
        
        // Verify sensor exists
        Sensor sensor = SensorResource.lookupSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\", \"sensorId\": \"" + sensorId + "\"}")
                    .build();
        }
        
        List<SensorReading> sensorReadings = readings.getOrDefault(sensorId, new ArrayList<>());
        SensorReading deleted = null;
        
        for (SensorReading reading : sensorReadings) {
            if (reading.getReadingId().equals(readingId)) {
                deleted = reading;
                sensorReadings.remove(reading);
                break;
            }
        }
        
        if (deleted == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Reading not found\", \"readingId\": \"" + readingId + "\"}")
                    .build();
        }
        
        return Response.ok(deleted).build();
    }

    // Static method for other resources to access/delete readings
    public static void deleteSensorReadings(String sensorId) {
        readings.remove(sensorId);
    }

    public static Map<String, List<SensorReading>> getReadings() {
        return readings;
    }
}
