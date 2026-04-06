package com.campus.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.campus.exception.RoomNotEmptyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Exception mapper for RoomNotEmptyException.
 * Returns HTTP 409 Conflict status.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        int status = 409; // Conflict
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("status", status);
        errorNode.put("error", "Conflict");
        errorNode.put("message", exception.getMessage());
        errorNode.put("roomId", exception.getRoomId());
        
        return Response.status(status)
                .entity(errorNode)
                .header("Content-Type", "application/json")
                .build();
    }
}
