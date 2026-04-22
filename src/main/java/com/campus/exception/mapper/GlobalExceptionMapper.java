package com.campus.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global exception mapper for handling unexpected exceptions.
 * Returns HTTP 500 Internal Server Error status.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        int status = 500; // Internal Server Error
        
        LOGGER.log(Level.SEVERE, "Unexpected error: " + exception.getMessage(), exception);
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("status", status);
        errorNode.put("error", "Internal Server Error");
        errorNode.put("message", exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred");
        
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(errorNode.toString())
                .build();
    }
}
