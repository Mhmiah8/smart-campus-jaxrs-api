package com.campus.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.campus.exception.SensorUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Exception mapper for SensorUnavailableException.
 * Returns HTTP 503 Service Unavailable status.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        int status = 403; // Forbidden
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("status", status);
        errorNode.put("error", "Forbidden");
        errorNode.put("message", exception.getMessage());
        errorNode.put("sensorId", exception.getSensorId());
        errorNode.put("sensorStatus", exception.getSensorStatus());
        
        return Response.status(status)
                .entity(errorNode)
                .header("Content-Type", "application/json")
                .build();
    }
}
