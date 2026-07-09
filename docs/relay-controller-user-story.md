# Relay Controller US / Story

生成日期：2026-07-09

本文档基于当前 `relay-controller` 代码整理，面向需求评审、设计评审、联调交付和后续维护。Relay Controller 是 DevBridge / Relay Tunnel 系统的控制面服务，只负责 tunnel 元数据、授权凭证、端口策略、计量上报和 region/grid/namespace 约束，不负责真实流量转发。

## 1 价值描述

### 作为

DevBridge / Relay Tunnel 系统的控制面服务、内部控制台、Relay Gateway 以及 region 内部平台服务。

### 我要

通过统一的 Relay Controller API 创建、查询、更新、删除 tunnel，管理 tunnel port 策略，获取 tunnel 连接 JWT，接收 gateway/grid 的流量计量上报，并在服务端完成 namespace、region、grid、配额、过期、TLS 和流控约束。

### 从而

- 让控制台或内部服务可以稳定管理 tunnel 生命周期。
- 让 Relay Gateway 可以通过控制面查询端口策略和上报流量。
- 让每个 region 的 Relay Controller 只管理本 region 的 grid 与 tunnel。
- 让 namespace 之间的数据和资源配额清晰隔离。
- 让调用方拿到 tunnel 元数据和连接凭证时，不需要额外 token 接口。
- 让系统第一版保持小而清晰，避免提前引入数据面转发、复杂权限系统和过重依赖。

### 现状

- 当前项目已经实现 tunnel CRUD、tunnel port CRUD、gateway port policy、metering、JWT 签发、Redis token cache、Flyway 初始化、OpenAPI YAML 生成接口、mTLS profile、基础限流和每 namespace 10 个 active tunnel 配额。
- OpenAPI YAML 是接口契约源：`src/main/resources/static/openapi.yaml`。
- Maven 编译期使用 OpenAPI Generator 生成 Spring API interface，Controller 只实现生成接口，不手写 mapping。
- 数据库使用 MySQL，迁移使用 Flyway。
- Redis 只作为 JWT token cache，Redis 不可用时仍可重新签发 token。
- `SccCrypto` 当前是本地 stub：加密原样返回，解密时去掉 `{scc}` 前缀。

### 要求

- 所有业务接口统一前缀：`/open-api-inner/v1/relay-controller`。
- 控制台侧/namespace 侧接口必须传 `X-Namespace`。
- Relay Controller 实例只负责一个 configured region。
- tunnel 必须绑定到当前 region 下存在的 grid。
- 每个 namespace 默认最多 10 个 active tunnel。
- 已删除和已过期 tunnel 不计入 active tunnel 配额。
- 默认限流为每个 namespace 每分钟 120 次；无 namespace 时按客户端 IP 限流。
- mTLS 作为服务端 TLS 认证能力，通过 `mtls` profile 开启。
- tunnel id 与 tunnel code 必须满足当前编码规则：`tunnelCode` 是 40-bit long，`tunnelId` 是该 40-bit 值的 8 位 lowercase base32 编码。
- 删除采用软删除，保留历史标识和计量关联。
- 第一版不实现真实流量转发，不实现 WebSocket/WebTransport/TCP/HTTP body proxy。

## 2 功能描述

### 2.1 功能说明

#### 2.1.1 Tunnel 管理

- 创建 tunnel。
- 查询 namespace 下 active tunnel 列表。
- 查询单个 tunnel detail，并返回 JWT。
- 更新 tunnel 基础信息。
- 删除单个 tunnel。
- 删除 namespace 下所有 tunnel。

创建 tunnel 时支持：

- `name`
- `gridName`
- `cluster`
- `description`
- `expiration`，单位为小时，默认 72 小时。
- `type`，枚举：`bridge`、`env`，默认 `bridge`。

创建成功后返回：

- `tunnelId`
- `tunnelCode`
- `gridName`
- `expiration`
- `created`
- `url`
- `type`

#### 2.1.2 Tunnel detail 与 JWT

Tunnel detail 返回 tunnel 元数据和 `jwt` 对象：

- `tokenType`
- `token`
- `expiresIn`

JWT 用于 Relay Gateway 或连接侧识别 tunnel 权限。token TTL 取以下较小值：

- `relay.jwt.token.ttl-seconds`
- tunnel 剩余有效期

Redis 缓存命中时，`expiresIn` 取 Redis TTL，避免高估 token 剩余可用时间。

#### 2.1.3 Tunnel port 策略

Tunnel port 是 tunnel 的子资源，用于定义指定端口是否允许匿名访问：

- 创建端口策略。
- 查询端口策略列表。
- 查询单个端口策略。
- 更新端口策略。
- 删除单个端口策略。
- 删除 tunnel 下全部端口策略。

端口范围：`1-65535`。

未配置端口默认拒绝。

#### 2.1.4 Gateway port policy

Gateway 使用如下接口查询指定 grid/tunnel/port 的策略：

```text
GET /open-api-inner/v1/relay-controller/grids/{gridName}/tunnels/{tunnelId}/ports/{port}
```

该接口不传 `X-Namespace`，因为 Gateway 侧以 `gridName` 作为调用作用域。Relay Controller 会校验：

- grid 属于当前 region。
- tunnel 属于该 grid。
- tunnel 未删除。
- tunnel 未过期。
- port 策略存在。

#### 2.1.5 Metering

Gateway 或 grid 侧上报 tunnel 流量：

```text
POST /open-api-inner/v1/relay-controller/grids/{gridName}/metering
```

服务端校验：

- grid 属于当前 region。
- tunnelId 在当前 region 存在。
- tunnel 属于请求 grid。
- tunnel 未过期。
- request 中 `tunnelCode` 与 tunnel 记录一致。

上报成功后：

- 写入 `metering` 表。
- 增加 `tunnel.bandwidth_used`。

#### 2.1.6 安全与接入控制

- Namespace 通过 `X-Namespace` 传入。
- 服务端可通过 `mtls` profile 开启双向 TLS。
- 请求无可信客户端证书时，TLS 握手阶段失败，不进入 Controller。
- 业务接口有 in-memory fixed-window rate limit。
- JWT 使用 RSA 私钥签名。未配置私钥时，本地开发会生成临时 RSA key pair。

### 2.2 约束与依赖

#### 2.2.1 业务约束

- 一个 Relay Controller 实例只管理一个 `relay.region`。
- 创建、查询、更新、删除 tunnel 时必须限定当前 region。
- 每个 namespace 默认最多 10 个 active tunnel。
- active tunnel 定义：`deleted = 0` 且 `expiration > now`。
- 删除 tunnel 时同步删除 tunnel port 策略，并清理 Redis 中的 JWT token cache。
- 更新 tunnel expiration 时清理 Redis token cache，避免旧 token TTL 超出新 tunnel 生命周期。
- list 只返回 active tunnel。
- detail/update/port/metering 不接受 expired tunnel。
- delete 可以删除 expired tunnel。

#### 2.2.2 技术依赖

- Java 17。
- Spring Boot 3。
- Jetty embedded server。
- MySQL。
- Redis with Jedis client。
- MyBatis Plus。
- Flyway。
- Nimbus JOSE JWT。
- MapStruct。
- OpenAPI Generator Maven Plugin。

#### 2.2.3 运行配置

核心配置位于 `application.yml`：

```yaml
relay:
  domain: myhuaweicloud.com
  region: region-a
  default-expiration-hours: 72
  tunnel:
    max-per-namespace: 10
  rate-limit:
    enabled: true
    requests-per-minute: 120
  jwt:
    issuer: devbridge
    key-id: "1"
    private-key:
    token:
      ttl-seconds: 86400
```

mTLS 通过 `application-mtls.yml` 和 profile 启用：

```bash
SPRING_PROFILES_ACTIVE=dev,mtls
SERVER_SSL_KEY_STORE=file:/path/to/server.p12
SERVER_SSL_KEY_STORE_PASSWORD=changeit
SERVER_SSL_TRUST_STORE=file:/path/to/server-truststore.p12
SERVER_SSL_TRUST_STORE_PASSWORD=changeit
```

JWT 私钥建议使用 PKCS#8 PEM：

```yaml
relay:
  jwt:
    private-key: |
      -----BEGIN PRIVATE KEY-----
      ...
      -----END PRIVATE KEY-----
```

## 3 实现设计

### 3.1 总结设计描述

Relay Controller 采用轻量分层结构：

- `interfaces`：Controller、request、response、OpenAPI 生成接口实现、流控拦截器。
- `application`：应用服务，编排业务流程、事务、日志和跨领域调用。
- `domain`：核心模型、领域服务、repository interface。
- `infrastructure`：MySQL/Redis/JWT/config/MapStruct 实现。
- `common`：统一结果、错误码、通用异常、工具类。

整体设计原则：

- OpenAPI YAML 驱动接口定义，Controller 不手写 mapping。
- 应用服务负责流程编排，领域服务负责业务断言。
- Repository interface 放在 domain，MyBatis Plus 实现放在 infrastructure。
- 数据库字段使用 snake_case，Java 字段使用 camelCase。
- 尽量保留第一版低复杂度，避免提前引入 Spring Security、分布式锁、复杂权限模型。

### 3.1.1 代码分层与职责

#### Controller 层

Controller 位于：

- `interfaces/controller/TunnelController.java`
- `interfaces/controller/TunnelPortController.java`
- `interfaces/controller/MeteringController.java`

职责：

- 实现 Maven 编译期生成的 API interface。
- 接收请求参数和 body。
- 调用 application service。
- 返回统一 `Result<T>`。

#### Application 层

应用服务位于：

- `application/service/TunnelAppService.java`
- `application/service/TunnelPortAppService.java`
- `application/service/MeteringAppService.java`
- `application/service/LocalGridService.java`

职责：

- 处理 namespace、region、grid、配额、过期等业务流程。
- 调用 repository 和 domain service。
- 控制事务边界。
- 写关键业务日志。

#### Domain 层

领域服务位于：

- `domain/service/NamespaceService.java`
- `domain/service/TunnelDomainService.java`
- `domain/service/TunnelPortDomainService.java`
- `domain/service/TunnelCodeGenerator.java`
- `domain/service/JwtTokenService.java`

职责：

- namespace 必填与 trim。
- tunnel active/owned/grid/expired 校验。
- port 范围校验。
- tunnel code 与 tunnel id 生成。
- JWT token service 抽象。

#### Infrastructure 层

基础设施位于：

- `infrastructure/persistence`
- `infrastructure/redis`
- `infrastructure/security`
- `infrastructure/config`

职责：

- MyBatis Plus mapper 和 repository 实现。
- MapStruct domain/entity 转换。
- Redis token cache。
- JWT signing 和 private key loading。
- datasource password stub decrypt。
- 配置属性绑定。

### 3.2 业务流程

#### 3.2.1 创建 tunnel

1. Controller 接收 `POST /tunnels`。
2. 读取 `X-Namespace`。
3. `NamespaceService.requireNamespace` 校验 namespace。
4. 根据 `gridName` 调用 `LocalGridService.requireLocalGrid`，确认 grid 属于当前 region。
5. 按 `region + namespace` 进入创建锁，避免单实例并发突破 active tunnel 配额。
6. 查询当前 namespace 在当前 region 的 active tunnel 数量。
7. 达到 `relay.tunnel.max-per-namespace` 时拒绝。
8. 解析 type，空值默认 `bridge`。
9. 解析 expiration，空值默认 `relay.default-expiration-hours`。
10. 生成 40-bit `tunnelCode` 和 8 位 base32 `tunnelId`。
11. 检查 `tunnelCode` 和 `tunnelId` 唯一性。
12. 构造 tunnel URL：`{tunnelId}-{gridName}-{relay.domain}`。
13. 保存 tunnel。
14. 返回创建结果。

#### 3.2.2 查询 tunnel list

1. 校验 namespace。
2. 如果传入 `gridName`，校验该 grid 属于当前 region。
3. 查询当前 namespace、当前 region、未删除、未过期的 tunnel。
4. 返回 list item。

#### 3.2.3 查询 tunnel detail

1. 校验 namespace。
2. 查询当前 region 下的 tunnel。
3. 校验 tunnel 存在、未删除、属于 namespace、未过期。
4. 读取 Redis token cache。
5. 缓存可用则返回 cached token 和 Redis TTL。
6. 缓存不可用则重新签发 JWT，写入 Redis。
7. 返回 tunnel detail 和 jwt。

#### 3.2.4 更新 tunnel

1. 校验 namespace。
2. 查询并校验 owned active tunnel。
3. 局部更新 name、description、cluster、expiration、type。
4. expiration 变化时删除 Redis token cache。
5. 保存 tunnel。
6. 返回 true。

#### 3.2.5 删除 tunnel

1. 校验 namespace。
2. 查询并校验 tunnel 属于 namespace。
3. 软删 tunnel。
4. 删除 Redis token cache。
5. 删除 tunnel port 策略。
6. 返回 true。

#### 3.2.6 管理 tunnel port

创建：

1. 校验 namespace。
2. 查询 owned active tunnel。
3. 校验 port 范围。
4. 校验 allowAnonymous 非空。
5. 校验 tunnelCode + port 不重复。
6. 保存 tunnel_port。

查询/更新/删除：

1. 查询 owned active tunnel。
2. 校验 port。
3. 查询 tunnel_port。
4. 根据动作返回、更新或删除。

#### 3.2.7 Gateway 查询 port policy

1. 校验 `gridName` 是当前 region 的 grid。
2. 查询当前 region 下的 tunnel。
3. 校验 tunnel 属于请求 grid 且未过期。
4. 校验 port。
5. 查询 tunnel port policy。
6. 返回 gateway policy。

#### 3.2.8 Metering 上报

1. 校验 grid 属于当前 region。
2. 查询 tunnel。
3. 校验 tunnel 属于 grid 且未过期。
4. 校验 request.tunnelCode 与数据库一致。
5. 写入 `metering`。
6. 增加 `tunnel.bandwidth_used`。
7. 返回 accepted。

#### 3.2.9 API 流控

1. `WebMvcConfig` 将 `RateLimitInterceptor` 注册到 `/open-api-inner/v1/relay-controller/**`。
2. 每个请求进入 Controller 前先经过拦截器。
3. 流控 key 优先使用 `X-Namespace`，没有时使用客户端 IP。
4. 固定窗口为 60 秒。
5. 超过 `relay.rate-limit.requests-per-minute` 时返回 HTTP 429 和统一 `Result`。

### 3.3 关键算法介绍

#### 3.3.1 Tunnel code 与 tunnel id

- `tunnelCode` 是 40-bit 正整数。
- 生成范围：`1` 到 `(1L << 40) - 1`。
- 使用 `TunnelCodeGenerator.generate()` 生成 long。
- 使用 `Base32Utils.encode40Bit()` 编码为 8 位 lowercase base32。
- 创建时最多重试 5 次，防止随机碰撞。
- 同时检查 `tunnel_code` 和 `tunnel_id` 唯一性。

#### 3.3.2 Active tunnel 配额

active tunnel 定义：

```sql
deleted = 0 AND expiration > now
```

创建前查询：

```sql
COUNT(1)
FROM tunnel t
INNER JOIN grid g ON g.grid = t.grid_name
WHERE t.namespace = ?
  AND g.region = ?
  AND t.deleted = 0
  AND t.expiration > ?
```

单实例并发控制：

- 使用固定 64 段锁。
- key 为 `region + ":" + namespace`。
- hash 到某一段锁。
- 同一 namespace 一定进入同一把锁，保证当前 JVM 内 `count -> save` 不会并发穿透。
- 不同 namespace 命中同一段锁时只会短暂排队，不影响数据正确性。

多实例说明：

- 当前锁只保证单个 relay-controller 实例内不突破。
- 如果同一个 region 多实例部署并同时写同一个库，需要 Redis 分布式锁或数据库锁。

#### 3.3.3 JWT token TTL

TTL 计算：

```text
min(relay.jwt.token.ttl-seconds, tunnel.expiration - now)
```

如果 tunnel 已过期，拒绝签发 token。

缓存策略：

- Redis key：`jwt:token:{tunnelId}`。
- get 失败、Redis 不可用、token 缺失、TTL <= 0 时视为缓存 miss。
- set 失败不影响请求，直接返回新签发 token。
- delete 失败会打印 warn 日志。

#### 3.3.4 API 流控算法

- in-memory fixed-window。
- 窗口：60 秒。
- key：`X-Namespace` 或 remote IP。
- 每个 key 一个 `WindowCounter`。
- `allow(now, limit)` 内部同步，窗口过期后重置 count。
- 超限返回 HTTP 429。

#### 3.3.5 mTLS

- 默认 profile 不启用 TLS，端口 8080。
- `mtls` profile 启用 HTTPS 和 client cert required，默认端口 8443。
- 服务端使用 keystore 提供服务端证书。
- 服务端使用 truststore 信任客户端 CA。
- 客户端无可信证书时，TLS 握手失败，请求不会进入 Spring MVC。

### 3.4 关键代码

| 能力 | 关键代码 |
| --- | --- |
| 应用入口 | `RelayControllerApplication.java` |
| OpenAPI 生成接口实现 | `interfaces/controller/*Controller.java` |
| Tunnel 主流程 | `application/service/TunnelAppService.java` |
| Port 主流程 | `application/service/TunnelPortAppService.java` |
| Metering 主流程 | `application/service/MeteringAppService.java` |
| Region/grid 校验 | `application/service/LocalGridService.java` |
| Namespace 校验 | `domain/service/NamespaceService.java` |
| Tunnel 领域断言 | `domain/service/TunnelDomainService.java` |
| Port 领域断言 | `domain/service/TunnelPortDomainService.java` |
| Tunnel code/id | `domain/service/TunnelCodeGenerator.java`, `common/util/Base32Utils.java` |
| JWT 签名 | `infrastructure/security/JwtSigner.java` |
| JWT private key | `infrastructure/security/JwtKeyProvider.java` |
| JWT 缓存 | `infrastructure/redis/JwtTokenCache.java` |
| 流控 | `interfaces/rate/RateLimitInterceptor.java`, `interfaces/config/WebMvcConfig.java` |
| 数据库访问 | `infrastructure/persistence/repository/*RepositoryImpl.java` |
| MyBatis XML | `src/main/resources/mapper/TunnelMapper.xml` |
| Flyway | `src/main/resources/db/migration` |
| mTLS profile | `src/main/resources/application-mtls.yml` |

### 3.5 接口定义及变更

接口统一前缀：

```text
/open-api-inner/v1/relay-controller
```

接口契约源：

```text
src/main/resources/static/openapi.yaml
```

OpenAPI YAML 也作为静态资源暴露：

```text
GET /openapi.yaml
```

#### 3.5.1 Tunnel API

| Method | Path | 说明 | Namespace |
| --- | --- | --- | --- |
| POST | `/tunnels` | 创建 tunnel | `X-Namespace` 必填 |
| GET | `/tunnels?gridName=` | 查询 active tunnel list | `X-Namespace` 必填 |
| DELETE | `/tunnels` | 删除 namespace 下所有 tunnel | `X-Namespace` 必填 |
| GET | `/tunnels/{tunnelId}` | 查询 tunnel detail 和 JWT | `X-Namespace` 必填 |
| PUT | `/tunnels/{tunnelId}` | 更新 tunnel | `X-Namespace` 必填 |
| DELETE | `/tunnels/{tunnelId}` | 删除 tunnel | `X-Namespace` 必填 |

#### 3.5.2 Tunnel Port API

| Method | Path | 说明 | Namespace |
| --- | --- | --- | --- |
| POST | `/tunnels/{tunnelId}/ports` | 创建端口策略 | `X-Namespace` 必填 |
| GET | `/tunnels/{tunnelId}/ports` | 查询端口策略列表 | `X-Namespace` 必填 |
| DELETE | `/tunnels/{tunnelId}/ports` | 删除全部端口策略 | `X-Namespace` 必填 |
| GET | `/tunnels/{tunnelId}/ports/{port}` | 查询单个端口策略 | `X-Namespace` 必填 |
| PUT | `/tunnels/{tunnelId}/ports/{port}` | 更新端口策略 | `X-Namespace` 必填 |
| DELETE | `/tunnels/{tunnelId}/ports/{port}` | 删除端口策略 | `X-Namespace` 必填 |

#### 3.5.3 Gateway / Metering API

| Method | Path | 说明 | Namespace |
| --- | --- | --- | --- |
| GET | `/grids/{gridName}/tunnels/{tunnelId}/ports/{port}` | Gateway 查询端口策略 | 不需要 |
| POST | `/grids/{gridName}/metering` | Gateway/grid 上报计量 | 不需要 |

#### 3.5.4 统一响应

成功：

```json
{
  "error_code": "0000",
  "error_message": "success",
  "data": {}
}
```

失败：

```json
{
  "error_code": "10006",
  "error_message": "active tunnel quota exceeded: max=10",
  "data": null
}
```

关键错误码：

| error_code | 说明 |
| --- | --- |
| `0000` | success |
| `40000` | parameter invalid |
| `40100` | unauthorized |
| `10001` | grid not found |
| `10002` | tunnel not found |
| `10003` | tunnel id conflict |
| `10004` | tunnel expired |
| `10005` | tunnel access denied |
| `10006` | tunnel quota exceeded |
| `11001` | tunnel port invalid |
| `11002` | tunnel port already exists |
| `11003` | tunnel port not found |
| `11004` | tunnel port access denied |
| `30001` | jwt generate failed |
| `30002` | jwt key invalid |
| `40001` | metering report failed |
| `42900` | rate limited |
| `50000` | internal error |

#### 3.5.5 接口变更记录

当前代码已完成以下接口收敛：

- 删除独立 token 接口，JWT 放到 tunnel detail 返回。
- 删除 stats 接口。
- 删除 node 同步接口。
- 删除证书获取接口。
- 删除一次性 token 接口。
- `tunnel` 与 `port` 资源路径使用复数：`tunnels`、`ports`。
- delete all 使用资源集合 DELETE：`DELETE /tunnels`、`DELETE /tunnels/{tunnelId}/ports`。
- API 前缀统一为 `/open-api-inner/v1/relay-controller`。
- Controller mapping 由 OpenAPI YAML 生成接口提供。

### 3.6 数据库表设计及数据割接设计

#### 3.6.1 表设计

##### grid

用途：记录 grid 与 region 的归属关系。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `_id` | 主键 |
| `grid` | grid name，唯一 |
| `region` | 所属 region |
| `created_at` | 创建时间，Unix seconds |
| `updated_at` | 更新时间，Unix seconds |

索引：

- `uk_grid(grid)`
- `idx_region(region)`

##### tunnel

用途：记录 tunnel 元数据。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `_id` | 主键 |
| `name` | tunnel 名称 |
| `tunnel_id` | base32 encoded 40-bit tunnel code |
| `tunnel_code` | 40-bit long |
| `grid_name` | 绑定 grid |
| `expiration` | 过期 Unix seconds |
| `namespace` | namespace |
| `description` | 描述 |
| `cluster` | cluster |
| `bandwidth_used` | 累计使用字节 |
| `url` | tunnel URL |
| `type` | `bridge` 或 `env` |
| `deleted` | 软删标记 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

索引：

- `uk_tunnel_id(tunnel_id)`
- `uk_tunnel_code(tunnel_code)`
- `idx_namespace(namespace)`
- `idx_grid_name(grid_name)`
- `idx_namespace_deleted(namespace, deleted)`

##### tunnel_port

用途：记录 tunnel 的端口访问策略。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `_id` | 主键 |
| `tunnel_code` | tunnel code |
| `port` | 端口，业务范围 1-65535 |
| `allow_anonymous` | 是否允许匿名访问 |

索引：

- `uk_tunnel_code_port(tunnel_code, port)`
- `idx_tunnel_code(tunnel_code)`
- `idx_port(port)`

##### metering

用途：记录 gateway/grid 上报的流量计量。

关键字段：

| 字段 | 说明 |
| --- | --- |
| `_id` | 主键 |
| `grid_name` | grid name |
| `tunnel_code` | tunnel code |
| `tunnel_id` | tunnel id |
| `usage_bytes` | 上报字节数 |
| `reported_at` | 上报时间 |
| `created_at` | 创建时间 |

索引：

- `idx_grid_name(grid_name)`
- `idx_tunnel_id(tunnel_id)`
- `idx_tunnel_code(tunnel_code)`
- `idx_reported_at(reported_at)`

#### 3.6.2 Flyway

迁移文件：

- `V1__init_schema.sql`：创建 `grid`、`tunnel`、`metering`、`tunnel_port`，并初始化 `grid-a`。
- `V2__clarify_tunnel_id_comments.sql`：修正 `tunnel_id` 字段注释为 base32 语义。

配置：

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1
    validate-on-migrate: true
```

#### 3.6.3 数据割接设计

当前第一版按新库新表落地，默认没有历史数据割接。

如果接入已有库：

1. 确认已有表结构是否与 Flyway V1 一致。
2. 如果已有空表或已有基础结构，使用 `baseline-on-migrate=true` 将现有库纳入 Flyway 管理。
3. 确保 `tunnel_id` 是 base32 语义，不再使用 hex 语义。
4. 确保历史 tunnel 的 `tunnel_code` 与 `tunnel_id` 可互相对应。
5. 对已删除数据设置 `deleted = 1`，不要物理删除历史 tunnel。
6. 如果历史 port 策略存在，按 `tunnel_code + port` 写入 `tunnel_port`。
7. 如果历史计量存在，按 `metering` 表结构导入，保留 `reported_at`。

割接校验：

- `grid.region` 与 `relay.region` 一致。
- 同一 `tunnel_id` 唯一。
- 同一 `tunnel_code` 唯一。
- 同一 tunnel 下同一 port 唯一。
- active tunnel 数量不超过 namespace 配额，或临时调大 `relay.tunnel.max-per-namespace` 完成迁移后再收紧。

## 5 开发者测试

### 5.1 测试建议

#### 5.1.1 本地依赖

准备：

- MySQL。
- Redis。
- JDK 17。
- Maven。

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS relay_controller
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;
```

启动：

```bash
mvn spring-boot:run
```

mTLS 启动：

```bash
SPRING_PROFILES_ACTIVE=dev,mtls mvn spring-boot:run
```

#### 5.1.2 手工测试建议

建议按以下顺序测试：

1. `GET /openapi.yaml`。
2. 创建 tunnel。
3. 查询 list。
4. 查询 detail，确认返回 JWT。
5. 创建 tunnel port。
6. 查询 port list/detail。
7. Gateway 查询 port policy。
8. Metering 上报。
9. 更新 tunnel expiration，确认 JWT cache 被清理。
10. 删除 port。
11. 删除 tunnel。
12. 删除全部 tunnels。

负向测试：

- 不传 `X-Namespace`。
- 使用不存在的 grid。
- 使用其他 region 的 grid。
- 使用 invalid tunnel type。
- 创建第 11 个 active tunnel。
- 使用无效 port：0、70000、null。
- 查询 expired tunnel。
- meter request 中 tunnelCode 与 tunnelId 不匹配。
- 不带客户端证书访问 mTLS 服务。
- 高频请求触发限流。

#### 5.1.3 HTTP smoke test

项目提供：

```bash
bash scripts/http-smoke-test.sh
```

IDEA scratch 位于本地 scratches，可用于联调当前 API。

### 5.2 单元测试

当前测试覆盖：

| 测试类 | 覆盖内容 |
| --- | --- |
| `Base32UtilsTest` | 40-bit base32 编码 |
| `TunnelCodeGeneratorTest` | tunnel code/id 生成 |
| `TunnelAppServiceTest` | tunnel 创建、配额、并发配额、更新、删除、list |
| `TunnelPortAppServiceTest` | tunnel port 创建、重复、校验、查询、更新、删除 |
| `MeteringAppServiceTest` | metering 上报与 tunnel bandwidth 累加 |
| `JwtTokenServiceTest` | JWT cache 命中、重新签发、TTL |
| `JwtTokenCacheTest` | Redis token cache 与 SccCrypto stub |
| `SccCryptoTest` | `{scc}` stub decrypt 行为 |
| `RateLimitInterceptorTest` | API 固定窗口限流 |
| `RelayControllerApiTest` | Controller 层请求、统一 Result、参数校验 |

开发者提交前建议执行：

```bash
mvn test
```

如果改动 OpenAPI YAML，还需要确认：

- Maven generate-sources 能生成 API interface。
- Controller 仍能实现生成接口。
- `openapi.yaml` 中接口路径、request/response schema 与实际 request/response class 一致。

如果改动数据库：

- 新增 Flyway migration，不修改已发布 migration。
- 保持 Java entity 字段 camelCase。
- 保持数据库字段 snake_case。
- 检查 MyBatis Plus `map-underscore-to-camel-case` 是否可映射。

如果改动 JWT：

- 验证无 private key 时本地临时 key 可启动。
- 验证 PKCS#8 PEM private key 可解析。
- 验证 token TTL 不超过 tunnel 剩余有效期。
- 验证更新 tunnel expiration 后 token cache 被 evict。

如果改动 mTLS：

- 验证普通 profile 仍可 HTTP 8080 启动。
- 验证 `mtls` profile 使用 HTTPS 8443。
- 验证不带客户端证书时 TLS 握手失败。
- 验证带受信任客户端证书时可访问 API。
