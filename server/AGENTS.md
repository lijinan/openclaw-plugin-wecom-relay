# OpenClaw Relay Server - Agent Guidelines

## Project Overview

Spring Boot relay server for OpenClaw WeCom plugin. Receives webhook requests from WeCom and forwards them through WebSocket tunnels to local OpenClaw instances.

**Technology Stack:**
- Java 1.8
- Spring Boot 2.7.18
- Maven
- WebSocket (Spring WebSocket)
- Lombok

**Architecture:**
```
WeCom Server → Relay Server (HTTP) → WebSocket Tunnel → Local Client → Local OpenClaw
```

## Build & Run Commands

### Development
```bash
# Build project
mvn clean install

# Run application (development profile)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run application (production profile)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Production
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/openclaw-relay-1.0.0.jar --spring.profiles.active=prod

# Restart script (if available)
./restart.sh
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ClassName

# Run tests with coverage
mvn clean test jacoco:report
```

## Project Structure

```
server/
├── src/main/java/com/openclaw/relay/
│   ├── RelayApplication.java           # Main application entry point
│   ├── config/
│   │   ├── RelayConfig.java            # Relay configuration
│   │   └── WebSocketConfig.java        # WebSocket configuration
│   ├── controller/
│   │   ├── HealthController.java       # Health check endpoint
│   │   └── RelayProxyController.java   # HTTP relay proxy controller
│   ├── model/
│   │   ├── ClientMessage.java          # Client → Server message model
│   │   ├── ServerMessage.java          # Server → Client message model
│   │   ├── VerifyPayload.java          # Client verification payload
│   │   └── WebhookPayload.java         # Webhook request payload
│   ├── service/
│   │   ├── MessageBufferService.java   # Message buffering service
│   │   └── PendingMessageManager.java  # Pending message timeout manager
│   └── websocket/
│       └── RelayWebSocketHandler.java  # WebSocket message handler
├── src/main/resources/
│   ├── application.yml                 # Base configuration
│   ├── application-dev.yml            # Development configuration
│   ├── application-prod.yml           # Production configuration
│   └── application-sample.yml         # Configuration template
├── target/                            # Build output
├── pom.xml                            # Maven build configuration
└── restart.sh                         # Restart script
```

## Configuration

Configuration files are located in `src/main/resources/`:

- **application.yml**: Base configuration
- **application-dev.yml**: Development overrides
- **application-prod.yml**: Production overrides
- **application-sample.yml**: Configuration template with examples

**Key Configuration Properties:**

```yaml
server:
  port: 8080                          # HTTP server port

wecom:
  relay:
    clients:                          # Client authentication
      client-id: "auth-token"
    timeout: 60000                    # Request timeout (ms)
```

**Environment Variables:**
- `SERVER_PORT`: Override server port
- `PROFILES`: Spring profiles (dev/prod)

## Code Style & Conventions

### Java Coding Standards
- **Language:** Java 1.8
- **Style:** Follow Spring Boot conventions
- **Logging:** Use SLF4J with Lombok's `@Slf4j` annotation
- **Dependencies:** Use Lombok for boilerplate reduction (@Data, @Builder, @Slf4j)

### Code Organization
- **Package structure:** `com.openclaw.relay.*`
- **Controller layer:** Handle HTTP endpoints
- **Service layer:** Business logic
- **Model layer:** Data structures
- **Config layer:** Configuration beans

### Naming Conventions
- Classes: `PascalCase` (e.g., `RelayProxyController`)
- Methods: `camelCase` (e.g., `sendMessageToClient`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `HOP_BY_HOP_HEADERS`)
- Private fields: `camelCase` (e.g., `webSocketHandler`)

### Logging Best Practices
- Use `@Slf4j` annotation
- Log at appropriate levels:
  - `ERROR`: Critical failures
  - `WARN`: Recoverable issues
  - `INFO`: Important events (startup, shutdown)
  - `DEBUG`: Detailed debugging info

### Error Handling
- Use appropriate HTTP status codes
- Return meaningful error messages
- Handle `InterruptedException` properly (restore interrupt flag)
- Use `CompletableFuture` for async operations

## Testing Guidelines

### Test Structure
- Test classes mirror source structure
- Use Spring Boot Test framework
- Mock external dependencies

### Test Categories
- Unit tests: Test individual components
- Integration tests: Test component interactions
- End-to-end tests: Test full request flow

### Running Tests
```bash
# All tests
mvn test

# With coverage
mvn clean test jacoco:report
```

## WebSocket Protocol

### Message Flow
1. Client connects to `/relay/ws/relay`
2. Client sends `register` message with `clientId` and `authToken`
3. Server validates client and registers connection
4. Server sends `heartbeat` messages periodically
5. Server sends `webhook` messages when receiving HTTP requests
6. Client sends `response` messages with webhook results

### Message Types
- **register**: Client authentication
- **webhook**: Server → Client webhook payload
- **response**: Client → Server webhook response
- **heartbeat**: Keep-alive ping

## HTTP Relay Proxy

### Proxy Endpoint
`POST /relay/client/{clientId}/**`

### Request Headers
- `X-Relay-Token`: Authentication token (required if configured)

### Response Codes
- `200`: Successful relay
- `401`: Unauthorized (invalid token)
- `405`: Method not allowed
- `503`: No connected client or send failed
- `504`: Gateway timeout (client response timeout)

### Proxy Behavior
- Forwards all HTTP methods (GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS)
- Preserves headers (except hop-by-hop headers)
- Encodes request body in Base64 (includes text body for debugging)
- Returns original response status, headers, and body

## Development Workflow

### Adding New Features
1. Create/update model classes in `model/`
2. Implement business logic in `service/`
3. Add controller endpoints in `controller/`
4. Update configuration if needed
5. Write tests
6. Run `mvn test` to verify

### Debugging
- Use `application-dev.yml` for development settings
- Enable DEBUG logging in config
- Use Spring Boot DevTools (if configured)
- Check logs in `target/` or configured log directory

### Common Issues

**WebSocket connection failures:**
- Check client authentication in `RelayConfig`
- Verify WebSocket URL in client configuration
- Check firewall/network restrictions

**Proxy timeout errors:**
- Increase `wecom.relay.timeout` in configuration
- Check client response time
- Verify network connectivity

**Build failures:**
- Ensure Java 1.8 is installed
- Check Maven version compatibility
- Clean and rebuild: `mvn clean install`

## Deployment

### Production Checklist
- Use `application-prod.yml` configuration
- Set appropriate logging levels
- Configure proper timeout values
- Enable HTTPS (use reverse proxy like Nginx)
- Set up monitoring and alerting
- Configure backup and recovery

### Health Check
Endpoint: `GET /actuator/health` (if Spring Actuator is enabled)

### Monitoring
- Monitor WebSocket connections
- Track proxy request/response times
- Monitor error rates
- Check client connection status

## Security Considerations

- Always use authentication tokens in production
- Enable HTTPS for all endpoints
- Validate all input data
- Use secure WebSocket (wss://) in production
- Do not log sensitive data (tokens, passwords)
- Implement rate limiting if needed

## Maintenance

### Version Updates
- Update dependencies in `pom.xml`
- Test thoroughly after updates
- Update this document with any breaking changes

### Code Review Checklist
- [ ] Code follows Spring Boot conventions
- [ ] Proper error handling
- [ ] Logging at appropriate levels
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Security review completed

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/)
- [Spring WebSocket Guide](https://docs.spring.io/spring-framework/docs/5.3.31/reference/html/web.html#websocket)
- [Maven Documentation](https://maven.apache.org/guides/)
