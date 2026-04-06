package com.campus.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.campus.exception.LinkedResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Exception mapper for LinkedResourceNotFoundException.
 * Returns HTTP 404 Not Found status.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        int status = 422; // Unprocessable Entity
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("status", status);
        errorNode.put("error", "Unprocessable Entity");
        errorNode.put("message", exception.getMessage());
        errorNode.put("resourceType", exception.getResourceType());
        errorNode.put("resourceId", exception.getResourceId());
        
        return Response.status(status)
                .entity(errorNode)
                .header("Content-Type", "application/json")
                .build();
    }
}
