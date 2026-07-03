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
- Nimbus JOSE JWT

## Implemented APIs

```text
POST   /tunnel
GET    /tunnels
GET    /tunnel?tunnelId=
PUT    /tunnel
DELETE /tunnel?tunnelId=

POST   /{gridname}/node/register
GET    /{gridname}/node?node_id=

GET    /{gridname}/config
POST   /{gridname}/metering
GET    /tunnel/status?tunnelId=
```

User tunnel APIs read `X-User-Id` and resolve `namespace = ns-{userId}`.

## Database

Create the MySQL schema and seed `grid-a`:

```bash
mysql -uroot -proot relay_controller < src/main/resources/db/schema.sql
```

## Run

Configure MySQL and Redis in `src/main/resources/application.yml`, then run:

```bash
mvn spring-boot:run
```

Use JDK 17 for normal development and deployment. The project uses Jetty instead of Tomcat and Jedis instead of Lettuce/Netty to avoid JDK 26 startup warnings from Tomcat native loading and Netty `Unsafe` access. If Maven itself is run on JDK 26, Maven's own Jansi/Guava dependencies may still print JVM warnings before the application starts; those are not emitted by the Relay Controller runtime.

If no RSA private key is configured, the service generates an ephemeral RSA key pair at startup for development. Configure `relay.jwt.private-key` and `relay.jwt.public-keys` for stable production keys.
