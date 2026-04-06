package com.campus.exception;

/**
 * Exception thrown when attempting to add a reading to a sensor that is not available.
 * (e.g., sensor under maintenance)
 */
public class SensorUnavailableException extends Exception {
    private String sensorId;
    private String sensorStatus;

    public SensorUnavailableException(String sensorId, String sensorStatus) {
        super("Sensor " + sensorId + " is not available (status: " + sensorStatus + ")");
        this.sensorId = sensorId;
        this.sensorStatus = sensorStatus;
    }

    public SensorUnavailableException(String message, String sensorId, String sensorStatus) {
        super(message);
        this.sensorId = sensorId;
        this.sensorStatus = sensorStatus;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getSensorStatus() {
        return sensorStatus;
    }
}
