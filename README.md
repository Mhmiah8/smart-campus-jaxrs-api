# Smart Campus API - JAX-RS REST API

A comprehensive REST API for managing smart campus rooms and sensors, built with JAX-RS (Jersey) using only in-memory storage (ConcurrentHashMap).

## Correct Maven Project Structure

```
smart-campus-api/
├── pom.xml                                          (Maven configuration)
├── README.md                                        (This file)
└── src/
    └── main/                                        (Main source directory)
        ├── java/                                    (Java source files)
        │   └── com/
        │       └── campus/
        │           ├── SmartCampusApplication.java
        │           ├── model/
        │           │   ├── Room.java
        │           │   ├── Sensor.java
        │           │   └── SensorReading.java
        │           ├── resource/
        │           │   ├── DiscoveryResource.java
        │           │   ├── RoomResource.java
        │           │   ├── SensorResource.java
        │           │   └── SensorReadingResource.java
        │           ├── exception/
        │           │   ├── RoomNotEmptyException.java
        │           │   ├── LinkedResourceNotFoundException.java
        │           │   ├── SensorUnavailableException.java
        │           │   └── mapper/
        │           │       ├── RoomNotEmptyExceptionMapper.java
        │           │       ├── LinkedResourceNotFoundExceptionMapper.java
        │           │       ├── SensorUnavailableExceptionMapper.java
        │           │       └── GlobalExceptionMapper.java
        │           └── filter/
        │               └── LoggingFilter.java
        └── webapp/                                  (Web content - static files, JSP, etc.)
            └── WEB-INF/                             (Deployment configuration)
                └── web.xml                          (Servlet deployment descriptor)
```


## Technologies Used

- **Framework**: JAX-RS 3.1.0 (Jakarta) with Jersey 3.1.3
- **Language**: Java 11+
- **Build Tool**: Maven 3.6+
- **Container**: Any Servlet 6.0 container (Tomcat 10+, GlassFish, WildFly)
- **JSON Processing**: Jackson 2.15.2
- **Storage**: ConcurrentHashMap (thread-safe in-memory)
- **Logging**: Java Util Logging

## Build Instructions

### Prerequisites
- JDK 11 or higher
- Maven 3.6 or higher
- Tomcat 10+ or other Servlet 6.0 container

### Clean Build
```bash
cd c:\Users\User\smart-campus-api
mvn clean package
```

This generates `target/smart-campus-api.war`

### Deploy to Tomcat
```bash
# Copy WAR file to Tomcat webapps
copy target\smart-campus-api.war %CATALINA_HOME%\webapps\

# Start Tomcat (Windows)
%CATALINA_HOME%\bin\startup.bat
```

The API will be available at: `http://localhost:8080/smart-campus-api/api/v1`

## API Endpoints Overview

### Base URL
```
http://localhost:8080/smart-campus-api/api/v1
```

### Discovery
- **GET** `/api/v1/` - API documentation and links

The discovery response includes `apiName`, `version`, `adminContact`, `description`, a `links` object, and an `endpoints` object. This is a simple HATEOAS-style entry point because it exposes navigable resource links from the API root.

### Rooms CRUD
- **GET** `/api/v1/rooms` - List all rooms (filter by building with ?building=name)
- **GET** `/api/v1/rooms/{roomId}` - Get specific room
- **POST** `/api/v1/rooms` - Create new room (201 + Location header)
- **PUT** `/api/v1/rooms/{roomId}` - Update room
- **DELETE** `/api/v1/rooms/{roomId}` - Delete room (fails if room has sensors)

### Sensors CRUD
- **GET** `/api/v1/sensors` - List all sensors (filter by type with ?type=TEMPERATURE)
- **GET** `/api/v1/sensors/{sensorId}` - Get specific sensor
- **POST** `/api/v1/sensors` - Create new sensor (201 + Location header, validates room exists)
- **PUT** `/api/v1/sensors/{sensorId}` - Update sensor
- **DELETE** `/api/v1/sensors/{sensorId}` - Delete sensor (cascades to readings)

### Sensor Readings (Sub-Resource)
- **GET** `/api/v1/sensors/{sensorId}/readings` - List readings for sensor
- **GET** `/api/v1/sensors/{sensorId}/readings/{readingId}` - Get specific reading
- **POST** `/api/v1/sensors/{sensorId}/readings` - Add reading (fails if sensor in maintenance)
- **DELETE** `/api/v1/sensors/{sensorId}/readings/{readingId}` - Delete reading

## HTTP Status Codes

| Code | Status | Usage |
|------|--------|-------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Invalid input |
| 403 | Forbidden | Sensor unavailable (maintenance/inactive) |
| 404 | Not Found | Resource not found |
| 422 | Unprocessable Entity | Linked resource invalid |
| 409 | Conflict | Business rule violation (room has sensors) |
| 500 | Internal Server Error | Unexpected error |

## Business Rules Implemented

### 1. Cannot Delete Room with Sensors
Attempting to delete a room that contains sensors returns **409 Conflict**.

### 2. Cannot Create Sensor with Invalid Room
A sensor must reference an existing room, or **422 Unprocessable Entity** is returned.

### 3. Cannot Add Reading to Unavailable Sensor
Readings can only be added to sensors with status "ACTIVE". Returns **403 Forbidden** otherwise.

### 4. Cascade Deletion
Deleting a sensor automatically deletes all its readings.

## Sample Data

The API includes pre-populated sample data:

**Rooms:**
- L1: Lecture Hall 1 (Engineering, Floor 1, 150 capacity)
- S101: Seminar 101 (Science, Floor 1, 30 capacity)
- L201: Lab 201 (Engineering, Floor 2, 50 capacity)

**Sensors:**
- T001: Temperature in L1 (ACTIVE)
- H001: Humidity in L1 (ACTIVE)
- T002: Temperature in S101 (MAINTENANCE)
- M001: Motion in L201 (ACTIVE)

**Readings:**
- T001 has 2 sample readings
- H001 has 1 sample reading

## Example API Calls

### Discovery Endpoint
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/
```

Response:
```json
{
  "apiName": "Smart Campus API",
  "version": "1.0",
  "adminContact": "admin@campus.edu",
  "links": {"rooms": "/api/v1/rooms", "sensors": "/api/v1/sensors"}
}
```

### List All Rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### Create Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"roomId\": \"S102\", \"name\": \"Seminar 102\", \"building\": \"Science\", \"floor\": 1, \"capacity\": 25}"
```

### List Sensors by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=TEMPERATURE"
```

### Create Sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"roomId\": \"L1\", \"type\": \"CO2\", \"status\": \"ACTIVE\", \"location\": \"Center\"}"
```

### Add Reading to Sensor
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/T001/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\": 23.5, \"unit\": \"°C\", \"timestamp\": \"2024-01-15T11:00:00\"}"
```

### Try to Delete Room with Sensors (Returns 409)
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/L1
```

Response:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete room L1 as it contains active sensors",
  "roomId": "L1"
}
```

## Logging

All HTTP requests and responses are logged with timing:
```
INFO: >>> REQUEST: [GET] /api/v1/rooms
INFO: <<< RESPONSE: [GET] /api/v1/rooms - Status: 200 (SUCCESS) - Duration: 5ms
```

## Answers to Coursework Questions

### Q1: Why ConcurrentHashMap instead of HashMap?
**A:** ConcurrentHashMap is thread-safe without full synchronization. Multiple threads can read simultaneously while updates are synchronized at bucket level. HashMap would corrupt data under concurrent access from multiple HTTP requests.

### Q1b: What is the JAX-RS resource lifecycle?
**A:** In this application, the JAX-RS application class is initialized once to register resources, filters, and exception mappers. Resource classes are typically instantiated by the framework when requests arrive, so request-specific state should not be stored in instance fields. This project keeps shared data in static in-memory maps to avoid per-request state leakage.

### Q2: How do exception mappers work?
**A:** JAX-RS invokes `ExceptionMapper<E>` implementations automatically. Each custom exception has a mapper that converts it to an HTTP response with appropriate status code:
- RoomNotEmptyException → 409 Conflict
- LinkedResourceNotFoundException → 422 Unprocessable Entity
- SensorUnavailableException → 403 Forbidden
- All others → 500 Internal Server Error

### Q2.1: IDs vs full objects in room list?
**A:** Returning only IDs reduces network bandwidth and response size, especially with many rooms. However, clients then need additional requests to fetch full details (N+1 problem). Returning full objects increases payload size but reduces client complexity and round trips. The choice depends on use case: listing views may use IDs, detailed views use full objects.

### Q3: Why use sub-resources for readings?
**A:** Sub-resources (`/sensors/{id}/readings`) provide semantic clarity, automatic validation, and cascading operations. The path structure clearly shows the relationship between sensors and readings.

### Q3.1: What happens with wrong Content-Type?
**A:** JAX-RS returns HTTP 415 Unsupported Media Type. The @Consumes annotation enforces JSON only. The server rejects the request without processing the payload, returning a clear error message.

### Q3.2: QueryParam vs PathParam for filtering?
**A:** Query parameters are superior for filtering because they're optional, can be combined with multiple filters, and maintain the collection resource URI structure. Path parameters suggest resource hierarchy and are better for identifying specific resources. Using query parameters allows flexible filtering without creating new endpoint variations.

### Q3b: What are the benefits of the sub-resource locator pattern?
**A:** It keeps the parent-child relationship explicit, reduces duplication in URI design, and allows each nested resource to enforce its own rules while still reusing the parent identifier.

### Q4: How does the logging filter work?
**A:** `LoggingFilter` implements `ContainerRequestFilter` and `ContainerResponseFilter`. Request filter logs the request and stores start time, response filter logs response with calculated duration.

### Q5: Why @JsonProperty annotations?
**A:** @JsonProperty maps Java field names to JSON property names consistently and provides documentation of the JSON structure.

### Q5.4: What information do stack traces reveal?
**A:** Stack traces expose internal paths, Java package names, framework versions, file names, line numbers, and potentially database structure. Attackers can use this information to identify vulnerabilities, craft targeted exploits, and map your application's architecture. Always sanitize error responses to show only necessary information.

### Q5.5: Why use JAX-RS filters for logging?
**A:** Filters separate cross-cutting concerns from business logic, ensuring consistent logging across all endpoints without code duplication. Manual logging would require modifying every resource method, increasing maintenance burden and risking inconsistency. Filters provide centralized, non-invasive logging that can be easily extended.

### Q6: How is API versioning handled?
**A:** The version is in the URI path (`/api/v1`) via `@ApplicationPath("/api/v1")`. Future versions can be added as `/api/v2` without breaking existing clients.

### Q6b: What is the benefit of HATEOAS-style links in the discovery endpoint?
**A:** The root resource gives clients the next available actions and entry points without hardcoding every URI. That makes the API easier to discover and less brittle if resource paths evolve.

### Q6c: Is DELETE idempotent in this API?
**A:** Yes. Repeating DELETE on the same room or sensor does not change server state after the first successful deletion. If the resource is already gone, the API returns `404 Not Found`, which is still consistent with idempotent semantics.

### Q7: What would you test?
**A:** 
- Unit tests for business logic
- Integration tests using Jersey test client
- Manual curl tests for all endpoints
- Concurrency tests with load
- All error scenarios and business rules

### Q8: How to add database support?
**A:** 
1. Add JPA dependencies
2. Create @Entity classes
3. Use EntityManager instead of ConcurrentHashMap
4. Implement DAO/Repository pattern
5. Add @Transactional management

### Q9: Security additions needed?
**A:**
1. JWT/OAuth2 authentication
2. RBAC authorization
3. HTTPS enforcement
4. Input validation/sanitization
5. Rate limiting
6. CORS configuration

### Q10: Production monitoring?
**A:**
1. Ship logs to ELK/Splunk
2. Health check endpoint
3. APM tools (DataDog, New Relic)
4. Distributed tracing with correlation IDs
5. Performance metrics and alerting

## Video Demo Checklist

1. GET / - Show API documentation
2. POST /rooms - Create room (201)
3. GET /rooms - List rooms
4. GET /rooms?building=Engineering - Filter by building
5. POST /sensors (valid room) - Create sensor (201)
6. POST /sensors (invalid room) - Create sensor (422 error)
7. GET /sensors - List all
8. GET /sensors?type=TEMPERATURE - Filter by type
9. POST /sensors/{id}/readings - Add reading to ACTIVE sensor (201)
10. POST /sensors/{id}/readings (T002 is MAINTENANCE) - Get 403 error
11. DELETE /sensors/{id} - Delete sensor with readings (cascade)
12. DELETE /rooms/{id} (has sensors) - Show 409 error
13. Console logs - Show request/response logging

## File Description

| File | Purpose |
|------|---------|
| SmartCampusApplication.java | JAX-RS Application configuration, registers resources and exception mappers |
| Room.java | Entity representing a campus room |
| Sensor.java | Entity representing a sensor with status (ACTIVE, MAINTENANCE, INACTIVE) |
| SensorReading.java | Entity representing a sensor data reading |
| DiscoveryResource.java | API documentation endpoint at / |
| RoomResource.java | CRUD operations for rooms, prevents delete if room has sensors |
| SensorResource.java | CRUD operations for sensors, validates room exists |
| SensorReadingResource.java | Sub-resource for readings, prevents adding to maintenance sensors |
| RoomNotEmptyException.java | Custom exception for business rule violation |
| LinkedResourceNotFoundException.java | Custom exception for missing referenced resource |
| SensorUnavailableException.java | Custom exception for unavailable sensors |
| RoomNotEmptyExceptionMapper.java | Maps to 409 Conflict |
| LinkedResourceNotFoundExceptionMapper.java | Maps to 422 Unprocessable Entity |
| SensorUnavailableExceptionMapper.java | Maps to 403 Forbidden |
| GlobalExceptionMapper.java | Catch-all mapper for unexpected exceptions (500) |
| LoggingFilter.java | Request/response logging with duration |
| web.xml | Servlet configuration, registers Jersey and LoggingFilter |

## Dependencies

- `jakarta.ws.rs:jakarta.ws.rs-api` - JAX-RS API specification
- `org.glassfish.jersey.*` - Jersey implementation
- `jakarta.servlet:jakarta.servlet-api` - Servlet API
- `com.fasterxml.jackson.core:jackson-databind` - JSON processing

---

