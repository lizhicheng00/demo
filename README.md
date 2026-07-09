# Relay Controller

Relay Controller is the DevBridge / Relay Tunnel control plane service. It manages tunnel metadata, namespace isolation, reusable JWT signing, metering reports, and port policies.

This service does not implement WebSocket, WebTransport, TCP, or HTTP body forwarding. Real traffic bridging belongs to Relay Gateway.

Detailed business and code summary: [docs/relay-controller-business-code-summary.md](docs/relay-controller-business-code-summary.md)

User story and implementation design: [docs/relay-controller-user-story.md](docs/relay-controller-user-story.md)

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

POST   /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
GET    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
GET    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
PUT    /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
GET    /open-api-inner/v1/relay-controller/grids/{gridName}/tunnels/{tunnelId}/ports/{port}
```

Namespace-scoped APIs read `X-Namespace` directly and store it as the tunnel namespace.
Each Relay Controller instance owns one configured region. Configure `relay.region`; tunnel, port, and metering operations only accept grids found under that local region.
Tunnel `type` is restricted to `bridge` or `env`; blank create requests default to `bridge`.
Tunnel `expiration` in create and update requests is a duration in hours. Blank create requests default to 72 hours. Responses still return expiration as Unix seconds.
Tunnel `tunnelCode` is a 40-bit `long`; `tunnelId` is the fixed 8-character lowercase base32 encoding of that 40-bit value.
Tunnel URL format is `{tunnelId}-{gridName}-{relay.domain}`.
Deleted tunnels are soft-deleted to preserve historical identifiers and metering references. List APIs return only active, non-expired tunnels. Detail, update, port, and metering operations reject expired tunnels; delete APIs can still delete expired tunnels.
Each namespace can own up to 10 active tunnels by default. Deleted and expired tunnels do not count against this quota. Configure `relay.tunnel.max-per-namespace` to change the limit.
Tunnel list responses expose stable metadata only: `tunnelId`, `tunnelCode`, `gridName`, `description`, `expiration`, `created`, and `url`. Runtime counters such as host/client connections or current upload/download rate require Gateway reporting and are intentionally not modeled here yet. Port policies remain available through the tunnel port APIs instead of being embedded into every list response.

Tunnel detail returns a `jwt` object so callers can get tunnel metadata and connection credentials in one request. Tokens are cached at `jwt:token:{tunnelId}` and expire at the earlier of `relay.jwt.token.ttl-seconds` or the tunnel expiration. `jwt.expiresIn` is the remaining lifetime of the returned token.

Tunnel port APIs manage the explicit per-port allow list for a tunnel. Unconfigured ports are denied by default. `allowAnonymous` only controls sending-side access to that port; listening-side gateway connection still requires token authentication.
The gateway port policy API keeps `gridName` in the path intentionally. Gateway callers use it as their grid scope, and Relay Controller verifies the tunnel belongs to that grid before returning the port policy.

Business APIs under `/open-api-inner/v1/relay-controller/**` have an in-memory fixed-window rate limit. The key is `X-Namespace` when present, otherwise client IP. The default is 120 requests per minute and can be adjusted with `relay.rate-limit.requests-per-minute` or disabled with `relay.rate-limit.enabled=false`.

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

For another local machine, prefer overriding only the datasource URL, username, and password:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/relay_controller?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true'
export SPRING_DATASOURCE_USERNAME='root'
export SPRING_DATASOURCE_PASSWORD='root'
mvn spring-boot:run
```

`SccCrypto` is currently a local test stub. It keeps encrypted values as plain text and strips a leading `{scc}` prefix on decrypt, so `SPRING_DATASOURCE_PASSWORD='{scc}root'` becomes `root` before datasource creation.

The following properties are decrypted before Spring Boot binds configuration:

```text
spring.datasource.password
spring.data.redis.password
relay.jwt.private-key
server.ssl.key-password
server.ssl.key-store-password
server.ssl.trust-store-password
```

Keep these values out of committed YAML and provide them through environment variables, deployment secrets, or encrypted config:

```text
SPRING_DATASOURCE_PASSWORD
SPRING_DATA_REDIS_PASSWORD
RELAY_JWT_PRIVATE_KEY
SERVER_SSL_KEY_STORE_PASSWORD
SERVER_SSL_TRUST_STORE_PASSWORD
```

For TLS, treat these as secrets:

```text
SERVER_SSL_KEY_STORE_PASSWORD
SERVER_SSL_TRUST_STORE_PASSWORD
```

The keystore/truststore files can also contain private material. Manage `SERVER_SSL_KEY_STORE` as sensitive storage because it contains the server private key. `SERVER_SSL_TRUST_STORE` usually contains only trusted client CA certificates, so it is less sensitive than the server keystore, but still keep it under controlled config management to avoid trusting the wrong CA. `SERVER_SSL_KEY_STORE_TYPE`, `SERVER_SSL_TRUST_STORE_TYPE`, `SERVER_PORT`, and path values themselves are not passwords.

TLS password values can use the local SCC stub prefix, for example `SERVER_SSL_KEY_STORE_PASSWORD='{scc}server-pass'`.

The project uses the official MySQL driver `com.mysql.cj.jdbc.Driver` with `mysql-connector-j`.
Do not set `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver` when using a `jdbc:mysql://` URL. If startup says the MariaDB driver cannot be loaded, remove that environment variable or external config override. Also make sure the JDBC URL uses the normal ASCII colon `jdbc:mysql://`, not the full-width Chinese colon `jdbc：mysql://`.

Run HTTP smoke tests against a running service:

```bash
bash scripts/http-smoke-test.sh
```

## Mutual TLS

Relay Controller can require client certificates at the embedded Jetty layer. Enable the `mtls` profile and provide a server keystore plus a truststore containing the client CA:

```bash
export SPRING_PROFILES_ACTIVE=dev,mtls
export SERVER_PORT=8443
export SERVER_SSL_KEY_STORE=file:/path/to/relay-controller-server.p12
export SERVER_SSL_KEY_STORE_PASSWORD='<secret>'
export SERVER_SSL_TRUST_STORE=file:/path/to/client-ca-truststore.p12
export SERVER_SSL_TRUST_STORE_PASSWORD='<secret>'
export RELAY_JWT_PRIVATE_KEY='<private-key-pem-or-{scc}encrypted-value>'
mvn spring-boot:run
```

Set `SERVER_SSL_KEY_ALIAS` only when the server keystore contains multiple aliases and you need to select one explicitly. With `server.ssl.client-auth=need`, requests without a trusted client certificate fail during the TLS handshake and never reach the API controllers. Callers must use `https://` and pass a client certificate signed by the CA in `SERVER_SSL_TRUST_STORE`.

Use JDK 17 for normal development and deployment. The project uses Jetty instead of Tomcat and Jedis instead of Lettuce/Netty to avoid JDK 26 startup warnings from Tomcat native loading and Netty `Unsafe` access. If Maven itself is run on JDK 26, Maven's own dependencies may still print JVM warnings before the application starts; those are not emitted by the Relay Controller runtime.

If no RSA private key is configured, the service generates an ephemeral RSA key pair at startup for development. Configure `RELAY_JWT_PRIVATE_KEY` for stable production token signing.
