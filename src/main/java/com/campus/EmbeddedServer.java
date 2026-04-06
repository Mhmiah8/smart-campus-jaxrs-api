package com.campus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import com.campus.SmartCampusApplication;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded Grizzly HTTP Server for Smart Campus API
 * Runs the JAX-RS application without needing Apache Tomcat
 * 
 * Usage: java -cp target/classes:target/dependency/* com.campus.EmbeddedServer
 */
public class EmbeddedServer {
    private static final Logger LOGGER = Logger.getLogger(EmbeddedServer.class.getName());
    
    // Base URI for the server
    public static final String BASE_URI = "http://0.0.0.0:8080/";
    
    /**
     * Starts the embedded Grizzly server
     * @return the running HttpServer instance
     * @throws IOException if server cannot start
     */
    public static HttpServer startServer() throws IOException {
        // Create resource configuration from the JAX-RS Application so /api/v1 is applied
        final ResourceConfig rc = ResourceConfig.forApplication(new SmartCampusApplication());
        
        // Create and start the server on specified URI
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), rc);
        
        LOGGER.log(Level.INFO, "======================================");
        LOGGER.log(Level.INFO, "Smart Campus API Server Started");
        LOGGER.log(Level.INFO, "======================================");
        LOGGER.log(Level.INFO, "Server running at: " + BASE_URI);
        LOGGER.log(Level.INFO, "API endpoint:     " + BASE_URI + "api/v1/");
        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO, "Available endpoints:");
        LOGGER.log(Level.INFO, "  Discovery:  GET  " + BASE_URI + "api/v1/");
        LOGGER.log(Level.INFO, "  Rooms:      GET  " + BASE_URI + "api/v1/rooms");
        LOGGER.log(Level.INFO, "  Sensors:    GET  " + BASE_URI + "api/v1/sensors");
        LOGGER.log(Level.INFO, "  Readings:   GET  " + BASE_URI + "api/v1/sensors/{sensorId}/readings");
        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO, "Press Ctrl+C to stop the server");
        LOGGER.log(Level.INFO, "======================================");
        
        return server;
    }

    /**
     * Main method - starts the server and waits for termination
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            
            // Keep the server running until interrupted
            System.in.read();
            
            // Graceful shutdown
            LOGGER.log(Level.INFO, "");
            LOGGER.log(Level.INFO, "Shutting down server...");
            server.shutdownNow();
            LOGGER.log(Level.INFO, "Server stopped.");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server initialization error: " + e.getMessage());
            System.exit(1);
        }
    }
}
