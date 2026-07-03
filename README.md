# Relay Controller

Relay Controller is the DevBridge / Relay Tunnel control plane service. It manages tunnel metadata, namespace isolation, gateway node registry, JWT signing/public key distribution, metering reports, and relay status lookup placeholders.

This service does not implement WebSocket, WebTransport, TCP, or HTTP body forwarding. Real traffic bridging belongs to Relay Gateway.

## Stack

- Java 17
- Spring Boot 3
- Jetty embedded server
- Maven
- MySQL
- Redis with Jedis client
- MyBatis Plus
- Springdoc OpenAPI
- Nimbus JOSE JWT

## Implemented APIs

```text
POST   /open-api-inner/v1/relay-controller/tunnel
GET    /open-api-inner/v1/relay-controller/tunnel/list?gridName=
GET    /open-api-inner/v1/relay-controller/tunnel?tunnelId=
PUT    /open-api-inner/v1/relay-controller/tunnel
DELETE /open-api-inner/v1/relay-controller/tunnel?tunnelId=

POST   /open-api-inner/v1/relay-controller/tunnel/{gridName}/node/register
GET    /open-api-inner/v1/relay-controller/tunnel/{gridName}/node?node_id=

GET    /open-api-inner/v1/relay-controller/tunnel/{gridName}/config
POST   /open-api-inner/v1/relay-controller/tunnel/{gridName}/metering
GET    /open-api-inner/v1/relay-controller/tunnel/status?tunnelId=

POST   /open-api-inner/v1/relay-controller/tokens/ott
POST   /open-api-inner/v1/relay-controller/tokens/rt
```

User tunnel APIs read `X-User-Id` and resolve `namespace = ns-{userId}`.

Token APIs are independent from tunnel resource paths. OTT is a 30-minute one-time token for RT exchange. RT is a 24-hour reusable token cached at `jwt:rt:{tunnelId}`. `POST /open-api-inner/v1/relay-controller/tokens/rt` accepts optional `X-Relay-Authorization` with either raw OTT or `Bearer <OTT>` format.

OpenAPI is maintained as YAML at `src/main/resources/static/openapi.yaml`. Maven uses this YAML during `generate-sources` to generate Spring API interfaces under `target/generated-sources/openapi`; controllers implement those generated interfaces and do not declare request mappings by hand.

Swagger UI also loads the same YAML directly:

```text
GET /swagger-ui/index.html
GET /openapi.yaml
```

## Database

Create the MySQL schema and seed `grid-a`:

```bash
mysql -uroot -proot relay_controller < src/main/resources/db/schema.sql
```

Database columns use snake_case for compound words, for example `tunnel_id`, `tunnel_code`, `grid_name`, `bandwidth_used`, and `register_time`. Java fields remain camelCase and rely on MyBatis Plus underscore-to-camel mapping, so entity classes do not carry redundant `@TableField` annotations.

## Run

Configure MySQL and Redis in `src/main/resources/application.yml`, then run:

```bash
mvn spring-boot:run
```

Use JDK 17 for normal development and deployment. The project uses Jetty instead of Tomcat and Jedis instead of Lettuce/Netty to avoid JDK 26 startup warnings from Tomcat native loading and Netty `Unsafe` access. If Maven itself is run on JDK 26, Maven's own dependencies may still print JVM warnings before the application starts; those are not emitted by the Relay Controller runtime.

If no RSA private key is configured, the service generates an ephemeral RSA key pair at startup for development. Configure `relay.jwt.private-key` and `relay.jwt.public-keys` for stable production keys.
