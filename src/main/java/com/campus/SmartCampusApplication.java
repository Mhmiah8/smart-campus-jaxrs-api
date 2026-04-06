package com.campus;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import com.campus.filter.LoggingFilter;
import com.campus.resource.*;
import com.campus.exception.mapper.*;

import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application class that configures the Smart Campus API.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Register resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(SensorReadingResource.class);
        
        // Register filters
        classes.add(LoggingFilter.class);
        
        // Register exception mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        
        return classes;
    }
}
