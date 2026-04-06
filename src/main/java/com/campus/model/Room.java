package com.campus.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Room model representing a physical room in the university campus.
 */
@XmlRootElement
public class Room {
    @JsonProperty("roomId")
    private String roomId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("building")
    private String building;
    
    @JsonProperty("floor")
    private int floor;
    
    @JsonProperty("capacity")
    private int capacity;

    public Room() {
    }

    public Room(String roomId, String name, String building, int floor, int capacity) {
        this.roomId = roomId;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", name='" + name + '\'' +
                ", building='" + building + '\'' +
                ", floor=" + floor +
                ", capacity=" + capacity +
                '}';
    }
}
