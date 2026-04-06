package com.campus.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SensorReading model representing a data point from a sensor.
 */
@XmlRootElement
public class SensorReading {
    @JsonProperty("readingId")
    private String readingId;
    
    @JsonProperty("sensorId")
    private String sensorId;
    
    @JsonProperty("value")
    private double value;
    
    @JsonProperty("unit")
    private String unit; // °C, %, m/s, lux, ppm
    
    @JsonProperty("timestamp")
    private String timestamp;

    public SensorReading() {
    }

    public SensorReading(String readingId, String sensorId, double value, String unit, String timestamp) {
        this.readingId = readingId;
        this.sensorId = sensorId;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public String getReadingId() {
        return readingId;
    }

    public void setReadingId(String readingId) {
        this.readingId = readingId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static String getCurrentTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "readingId='" + readingId + '\'' +
                ", sensorId='" + sensorId + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
