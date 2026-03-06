# OpenClaw Relay Local Client - Agent Guidelines

## Project Overview

Local relay client for OpenClaw WeCom plugin. Establishes WebSocket tunnel to remote server and forwards webhook requests to local OpenClaw instance.

**Technology Stack:**
- Node.js >= 18.0.0
- TypeScript 5.3+
- WebSocket (ws library)
- Axios (HTTP client)

**Architecture:**
```
Remote Server → WebSocket Tunnel → Local Client → Local OpenClaw
```

## Build & Run Commands

### Development
```bash
# Install dependencies
npm install

# Run in development mode with auto-reload
npm run dev

# Run in watch mode
npm run watch

# Build TypeScript
npm run build
```

### Production
```bash
# Build for production
npm run build

# Start production server
npm start
```

### Testing
```bash
# Run tests (if configured)
npm test

# Run with coverage (if configured)
npm run test:coverage
```

## Project Structure

```
local/
├── src/
│   ├── index.ts                    # Main entry point
│   ├── config.ts                   # Configuration loader
│   ├── relay-client.ts            # WebSocket client implementation
│   ├── openclaw-client.ts         # HTTP client for OpenClaw
│   └── types.ts                   # TypeScript type definitions
├── dist/                          # Compiled JavaScript output
├── node_modules/                  # Node.js dependencies
├── config.json                    # Runtime configuration (create from example)
├── config.example.json            # Configuration template
├── package.json                   # Node.js package manifest
├── tsconfig.json                  # TypeScript configuration
└── README.md                      # Project documentation
```

## Configuration

Configuration is loaded from `config.json` in the project root. Copy from the example:

```bash
cp config.example.json config.json
```

### Configuration Structure

```json
{
  "server": {
    "url": "wss://your-server.com/relay/ws/relay",
    "reconnectInterval": 5000,
    "maxReconnectAttempts": 0
  },
  "auth": {
    "clientId": "openclaw-local",
    "authToken": "your-auth-token-here"
  },
  "openclaw": {
    "baseUrl": "http://localhost:3000",
    "webhookPath": "/webhooks/wecom",
    "timeout": 60000
  },
  "heartbeat": {
    "interval": 30000,
    "timeout": 10000
  }
}
```

### Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `server.url` | WebSocket server URL | Required |
| `server.reconnectInterval` | Reconnect delay (ms) | 5000 |
| `server.maxReconnectAttempts` | Max reconnect attempts (0 = infinite) | 0 |
| `auth.clientId` | Client identifier | Required |
| `auth.authToken` | Authentication token | Required |
| `openclaw.baseUrl` | Local OpenClaw base URL | Required |
| `openclaw.webhookPath` | Default webhook path | `/webhooks/wecom` |
| `openclaw.timeout` | HTTP request timeout (ms) | 60000 |
| `heartbeat.interval` | Heartbeat interval (ms) | 30000 |
| `heartbeat.timeout` | Heartbeat response timeout (ms) | 10000 |

### Environment Variables

Environment variables override configuration file values:

| Environment Variable | Maps To |
|---------------------|---------|
| `RELAY_SERVER_URL` | `server.url` |
| `RELAY_CLIENT_ID` | `auth.clientId` |
| `RELAY_AUTH_TOKEN` | `auth.authToken` |
| `OPENCLAW_BASE_URL` | `openclaw.baseUrl` |
| `OPENCLAW_WEBHOOK_PATH` | `openclaw.webhookPath` |
| `CONFIG_PATH` | Path to config file |

## Code Style & Conventions

### TypeScript Standards
- **Language:** TypeScript 5.3+
- **Style:** Follow TypeScript best practices
- **Strict mode:** Enabled in `tsconfig.json`
- **Module system:** ESM

### Code Organization
- **Entry point:** `src/index.ts` - Application bootstrap
- **Configuration:** `src/config.ts` - Config loading and validation
- **WebSocket client:** `src/relay-client.ts` - Relay connection handling
- **HTTP client:** `src/openclaw-client.ts` - OpenClaw communication
- **Types:** `src/types.ts` - Shared TypeScript interfaces

### Naming Conventions
- Classes: `PascalCase` (e.g., `RelayClient`)
- Interfaces: `PascalCase` (e.g., `ServerMessage`)
- Functions/Methods: `camelCase` (e.g., `connect`, `handleMessage`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `HOP_BY_HOP_HEADERS`)
- Private members: `camelCase` (e.g., `ws`, `config`)

### Error Handling
- Use try-catch for async operations
- Log errors with context
- Graceful degradation on connection failures
- Proper cleanup on shutdown

### Logging
- Use `console.log` for informational messages
- Use `console.error` for errors
- Use `console.warn` for warnings
- Prefix logs with component name (e.g., `[RelayClient]`)

## WebSocket Protocol

### Connection Flow
1. Client connects to server WebSocket URL
2. Client sends `register` message with `clientId` and `authToken`
3. Server validates and responds with `registered` message
4. Client starts heartbeat mechanism
5. Server sends `webhook` messages when requests arrive
6. Client processes webhook and sends `response` message

### Message Types

**Client → Server:**
- `register`: Client authentication
- `ping`: Heartbeat keep-alive
- `response`: Webhook response

**Server → Client:**
- `registered`: Registration successful
- `webhook`: Webhook payload to process
- `pong`: Heartbeat response
- `error`: Error notification

### Message Formats

```typescript
// Register
{
  type: 'register',
  clientId: string,
  authToken: string
}

// Webhook
{
  type: 'webhook',
  messageId: string,
  payload: WebhookPayload
}

// Response
{
  type: 'response',
  messageId: string,
  payload: ResponsePayload,
  error?: string
}
```

## Development Workflow

### Adding New Features
1. Update TypeScript interfaces in `src/types.ts`
2. Implement logic in appropriate service class
3. Update configuration schema if needed
4. Test with `npm run dev`
5. Build and verify with `npm run build`

### Debugging
- Use `npm run dev` for development with auto-reload
- Enable detailed logging in console
- Check WebSocket connection status
- Verify OpenClaw is accessible

### Common Issues

**WebSocket connection failures:**
- Verify server URL is correct
- Check network connectivity
- Confirm authentication token matches server config
- Check firewall/proxy settings

**OpenClaw connection errors:**
- Verify OpenClaw is running
- Check `baseUrl` configuration
- Verify webhook path is correct
- Check timeout settings

**Build errors:**
- Ensure Node.js >= 18.0.0
- Run `npm install` to update dependencies
- Check TypeScript version compatibility

## Component Details

### RelayClient (src/relay-client.ts)

**Responsibilities:**
- WebSocket connection management
- Client registration and authentication
- Heartbeat mechanism
- Message routing to/from server
- Reconnection logic

**Key Methods:**
- `start()`: Initialize and connect
- `connect()`: Establish WebSocket connection
- `register()`: Send registration message
- `handleMessage()`: Process incoming messages
- `handleWebhook()`: Forward webhook to OpenClaw
- `stop()`: Clean shutdown

### OpenClawClient (src/openclaw-client.ts)

**Responsibilities:**
- HTTP communication with local OpenClaw
- Request/response handling
- Header filtering
- Content type detection
- Binary/text response handling

**Key Methods:**
- `forwardWebhook()`: Forward webhook payload to OpenClaw
- `healthCheck()`: Check OpenClaw availability
- `isTextContentType()`: Detect text content types

### Configuration (src/config.ts)

**Responsibilities:**
- Load configuration from file
- Apply environment variable overrides
- Provide type-safe configuration access

## Deployment

### Production Checklist
- Use `npm run build` to compile TypeScript
- Set up proper configuration file
- Configure systemd or process manager (PM2, etc.)
- Set environment variables as needed
- Enable logging aggregation
- Set up monitoring and alerting

### Process Management

**Using PM2:**
```bash
npm install -g pm2
npm run build
pm2 start dist/index.js --name openclaw-relay
pm2 save
pm2 startup
```

**Using systemd:**
```bash
sudo cp openclaw-relay.service /etc/systemd/system/
sudo systemctl enable openclaw-relay
sudo systemctl start openclaw-relay
```

### Health Monitoring

Monitor these metrics:
- WebSocket connection status
- Reconnection attempts
- Webhook processing latency
- OpenClaw response times
- Error rates

## Security Considerations

- Use secure WebSocket (wss://) in production
- Never commit `config.json` with real tokens
- Rotate authentication tokens regularly
- Use environment variables for sensitive data
- Validate all incoming data
- Implement rate limiting if needed
- Keep dependencies updated

## Testing Guidelines

### Test Structure
- Unit tests for individual components
- Integration tests for WebSocket communication
- End-to-end tests for full message flow

### Running Tests
```bash
# Run all tests
npm test

# Run with coverage
npm run test:coverage

# Watch mode
npm run test:watch
```

## Maintenance

### Version Updates
- Update dependencies in `package.json`
- Test thoroughly after updates
- Update this document with breaking changes
- Review changelog for security updates

### Code Review Checklist
- [ ] TypeScript compiles without errors
- [ ] Proper error handling
- [ ] Logging at appropriate levels
- [ ] Configuration schema updated
- [ ] Tests pass
- [ ] Documentation updated

## Troubleshooting

### Connection Issues
1. Check WebSocket URL in config
2. Verify server is accessible
3. Confirm authentication token
4. Check network/firewall settings
5. Review server logs

### Performance Issues
1. Monitor message queue length
2. Check OpenClaw response times
3. Review timeout settings
4. Check memory usage
5. Profile CPU usage

### Debug Mode
Enable verbose logging by modifying `console.log` statements or using a logging library with debug levels.

## Resources

- [WebSocket Protocol RFC](https://tools.ietf.org/html/rfc6455)
- [Node.js Documentation](https://nodejs.org/docs/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Axios Documentation](https://axios-http.com/docs/intro)
- [ws Library](https://github.com/websockets/ws)
