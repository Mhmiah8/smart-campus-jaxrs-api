package com.campus.exception;

/**
 * Exception thrown when a referenced resource is not found.
 * Used for cases such as invalid room ID when creating a sensor.
 */
public class LinkedResourceNotFoundException extends Exception {
    private String resourceType;
    private String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super("Referenced " + resourceType + " with ID '" + resourceId + "' not found");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public LinkedResourceNotFoundException(String message, String resourceType, String resourceId) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
