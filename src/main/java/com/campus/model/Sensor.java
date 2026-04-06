package com.campus.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sensor model representing a sensor deployed in a room.
 * Sensor types: TEMPERATURE, HUMIDITY, MOTION, LIGHT, CO2
 */
@XmlRootElement
public class Sensor {
    @JsonProperty("sensorId")
    private String sensorId;
    
    @JsonProperty("roomId")
    private String roomId;
    
    @JsonProperty("type")
    private String type; // TEMPERATURE, HUMIDITY, MOTION, LIGHT, CO2
    
    @JsonProperty("status")
    private String status; // ACTIVE, MAINTENANCE, INACTIVE
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("currentValue")
    private double currentValue;

    public Sensor() {
    }

    public Sensor(String sensorId, String roomId, String type, String status, String location) {
        this.sensorId = sensorId;
        this.roomId = roomId;
        this.type = type;
        this.status = status;
        this.location = location;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "sensorId='" + sensorId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                ", currentValue=" + currentValue +
                '}';
    }
}
