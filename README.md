# Relay Controller

Relay Controller is the DevBridge / Relay Tunnel control plane service. It manages tunnel metadata, namespace isolation, reusable JWT signing, metering reports, port policies, and relay status lookup placeholders.

This service does not implement WebSocket, WebTransport, TCP, or HTTP body forwarding. Real traffic bridging belongs to Relay Gateway.

## Stack

- Java 17
- Spring Boot 3
- Jetty embedded server
- Maven
- MySQL
- Redis with Jedis client
- MyBatis Plus
- Nimbus JOSE JWT

## Implemented APIs

```text
POST   /open-api-inner/v1/relay-controller/tunnels
GET    /open-api-inner/v1/relay-controller/tunnels?gridName=
DELETE /open-api-inner/v1/relay-controller/tunnels
GET    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}
PUT    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}

POST   /open-api-inner/v1/relay-controller/grids/{gridName}/metering
GET    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/status

POST   /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
GET    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
GET    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
PUT    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
GET    /open-api-inner/v1/relay-controller/grids/{gridName}/tunnels/{tunnelId}/ports/{port}

POST   /open-api-inner/v1/relay-controller/tokens
```

Namespace-scoped APIs read `X-Namespace` directly and store it as the tunnel namespace.
Each Relay Controller instance owns one configured region. Configure `relay.region`; tunnel, port, token, metering, and status operations only accept grids found under that local region.
Tunnel `type` is restricted to `bridge` or `env`; blank create requests default to `bridge`.
Tunnel `expiration` in create and update requests is a duration in hours. Blank create requests default to 72 hours. Responses still return expiration as Unix seconds.
Tunnel `tunnelCode` is a 40-bit `long`; `tunnelId` is the fixed 8-character lowercase base32 encoding of that 40-bit value.
Tunnel URL format is `{tunnelId}-{gridName}-{relay.domain}`.
Deleted tunnels are soft-deleted to preserve historical identifiers and metering references. List APIs return only active, non-expired tunnels. Detail, update, token, port, status, and metering operations reject expired tunnels; delete APIs can still delete expired tunnels.

Token APIs are independent from tunnel resource paths. `X-Namespace` is optional; when present, it enforces namespace ownership before issuing a token. Tokens are cached at `jwt:token:{tunnelId}` and expire at the earlier of `relay.jwt.token.ttl-seconds` or the tunnel expiration.

Tunnel port APIs manage the explicit per-port allow list for a tunnel. Unconfigured ports are denied by default. `allowAnonymous` only controls sending-side access to that port; listening-side gateway connection still requires token authentication.

OpenAPI is maintained as YAML at `src/main/resources/static/openapi.yaml`. Maven uses this YAML during `generate-sources` to generate Spring API interfaces under `target/generated-sources/openapi`; controllers implement those generated interfaces and do not declare request mappings by hand.
The same YAML is served directly as a static resource:

```text
GET /openapi.yaml
```

## Database

Create the MySQL database before starting the service:

```sql
CREATE DATABASE IF NOT EXISTS relay_controller
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;
```

Flyway runs on application startup and applies migrations from `src/main/resources/db/migration`. The initial migration creates `grid`, `tunnel`, `metering`, `tunnel_port`, and seeds `grid-a`.

Database columns use snake_case for compound words, for example `tunnel_id`, `tunnel_code`, `grid_name`, `bandwidth_used`, and `allow_anonymous`. Java fields remain camelCase and rely on MyBatis Plus underscore-to-camel mapping, so entity classes do not carry redundant `@TableField` annotations.

## Run

Configure MySQL and Redis in `src/main/resources/application.yml`, then run:

```bash
mvn spring-boot:run
```

Run HTTP smoke tests against a running service:

```bash
bash scripts/http-smoke-test.sh
```

Use JDK 17 for normal development and deployment. The project uses Jetty instead of Tomcat and Jedis instead of Lettuce/Netty to avoid JDK 26 startup warnings from Tomcat native loading and Netty `Unsafe` access. If Maven itself is run on JDK 26, Maven's own dependencies may still print JVM warnings before the application starts; those are not emitted by the Relay Controller runtime.

If no RSA private key is configured, the service generates an ephemeral RSA key pair at startup for development. Configure `relay.jwt.private-key` for stable production token signing.
