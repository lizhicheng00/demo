# Demo Java Starter

小型 Spring Boot 初始化项目，包含 JWT、RelayDomain 管理、Node/listening 同步、运营面统计、MySQL、Flyway、OpenAPI/Swagger 和统一错误码。

## 技术栈

- Java 21
- Spring Boot 3.3
- Spring Security + JWT
- Spring Data JPA
- MySQL 8
- Flyway
- springdoc-openapi Swagger UI

## 本地启动

```bash
docker compose up -d mysql
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

默认会在启动时创建管理员账号：

```text
username: admin
password: admin123
```

生产环境请覆盖这些环境变量：

```bash
export APP_JWT_SECRET='replace-with-at-least-32-characters-secret'
export APP_ADMIN_PASSWORD='strong-password'
```

## 主要接口

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/relay-domains`
- `GET /api/v1/relay-domains`
- `GET /api/v1/relay-domains/{id}`
- `PUT /api/v1/relay-domains/{id}`
- `PATCH /api/v1/relay-domains/{id}/status`
- `DELETE /api/v1/relay-domains/{id}`
- `POST /api/v1/nodes/sync`
- `POST /api/v1/nodes/{nodeCode}/heartbeat`
- `GET /api/v1/nodes`
- `POST /api/v1/listenings`
- `GET /api/v1/listenings/sync`
- `PATCH /api/v1/listenings/{id}/status`
- `GET /api/v1/ops/stats/overview`
- `GET /api/v1/ops/stats/traffic`

登录后在 Swagger 右上角 `Authorize` 输入：

```text
Bearer <token>
```
