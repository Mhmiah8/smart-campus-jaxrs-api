package com.campus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Logger;

/**
 * Discovery Resource providing API metadata and links to other endpoints.
 * Root endpoint: /api/v1
 */
@Path("")
public class DiscoveryResource {
    private static final Logger LOGGER = Logger.getLogger(DiscoveryResource.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        LOGGER.info("Discovery endpoint called");
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("apiName", "Smart Campus API");
        root.put("version", "1.0");
        root.put("adminContact", "admin@campus.edu");
        root.put("description", "REST API for managing smart campus rooms and sensors");
        
        ObjectNode links = mapper.createObjectNode();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("self", "/api/v1");
        
        root.set("links", links);
        
        ObjectNode endpoints = mapper.createObjectNode();
        
        ObjectNode roomsEndpoint = mapper.createObjectNode();
        roomsEndpoint.put("description", "Manage rooms in the campus");
        roomsEndpoint.put("methods", "GET (list all), POST (create new)");
        roomsEndpoint.put("url", "/api/v1/rooms");
        endpoints.set("rooms", roomsEndpoint);
        
        ObjectNode sensorsEndpoint = mapper.createObjectNode();
        sensorsEndpoint.put("description", "Manage sensors in rooms");
        sensorsEndpoint.put("methods", "GET (list all, filter by type), POST (create new)");
        sensorsEndpoint.put("url", "/api/v1/sensors");
        sensorsEndpoint.put("queryParams", "type (TEMPERATURE, HUMIDITY, MOTION, LIGHT, CO2)");
        endpoints.set("sensors", sensorsEndpoint);
        
        ObjectNode readingsEndpoint = mapper.createObjectNode();
        readingsEndpoint.put("description", "Manage sensor readings");
        readingsEndpoint.put("methods", "GET (list by sensor), POST (create new)");
        readingsEndpoint.put("url", "/api/v1/sensors/{sensorId}/readings");
        endpoints.set("readings", readingsEndpoint);
        
        root.set("endpoints", endpoints);
        
        return Response.ok(root).build();
    }
}
