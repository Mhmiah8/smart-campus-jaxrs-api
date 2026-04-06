package com.campus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Request/Response logging filter for the Smart Campus API.
 * Logs all incoming requests and outgoing responses with timing information.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
    private static final String REQUEST_START_TIME = "request-start-time";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Store request start time
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(REQUEST_START_TIME, startTime);
        
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        String query = requestContext.getUriInfo().getQueryParameters().isEmpty() ? 
            "" : "?" + requestContext.getUriInfo().getRequestUri().getRawQuery();
        
        LOGGER.log(Level.INFO, 
            String.format(">>> REQUEST: [%s] %s%s", method, path, query));
        
        // Log headers if present
        requestContext.getHeaders().forEach((key, values) -> {
            String joinedValues = String.join(", ", values.stream().map(Object::toString).toList());
            LOGGER.log(Level.FINE, String.format("    Header [%s]: %s", key, joinedValues));
        });
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object startProperty = requestContext.getProperty(REQUEST_START_TIME);
        long startTime = startProperty instanceof Long ? (Long) startProperty : System.currentTimeMillis();
        long duration = System.currentTimeMillis() - startTime;
        
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        int status = responseContext.getStatus();
        
        String statusType = status >= 200 && status < 300 ? "SUCCESS" : 
                           status >= 300 && status < 400 ? "REDIRECT" :
                           status >= 400 && status < 500 ? "CLIENT_ERROR" : "SERVER_ERROR";
        
        LOGGER.log(Level.INFO, 
            String.format("<<< RESPONSE: [%s] %s - Status: %d (%s) - Duration: %dms", 
                method, path, status, statusType, duration));
        
        // Log response headers if present
        responseContext.getHeaders().forEach((key, values) -> {
            String joinedValues = String.join(", ", values.stream().map(Object::toString).toList());
            LOGGER.log(Level.FINE, String.format("    Header [%s]: %s", key, joinedValues));
        });
    }
}
