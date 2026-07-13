# Relay Controller 业务与代码总结

生成日期：2026-07-07

本文档总结当前 `relay-controller` 的业务定位、核心模型、接口语义、代码结构、运行配置、数据持久化、测试覆盖和关键设计取舍。文档依据当前仓库代码整理，主要面向后续开发、维护、联调和架构评审。

## 1. 项目定位

Relay Controller 是 DevBridge / Relay Tunnel 系统中的控制面服务。它负责维护 tunnel 元数据、namespace 隔离、cluster/region 归属校验、JWT token 签发、端口访问策略和流量计量上报。

它不负责真实流量转发，也不实现 WebSocket、WebTransport、TCP 或 HTTP body 代理。真实流量转发属于 Relay Gateway。Relay Controller 的职责是给 Gateway、控制台或内部服务提供“能否访问、如何访问、策略是什么、元数据是什么”的控制面答案。

当前服务重点遵循几个边界：

- 一个 Relay Controller 实例只负责一个 configured region。
- tunnel 必须绑定到本 region 下的 cluster。
- namespace 来自调用方传入的 `X-Namespace`。
- tunnel id 是对 40-bit tunnel code 的 base32 表达。
- 删除采用软删，保留历史标识和计量关联。
- OpenAPI YAML 是接口契约源，Controller 继承 Maven 编译期生成的接口。
- 接口文档以静态 `openapi.yaml` 为准。

## 2. 系统角色与概念

### 2.1 Relay Controller

Relay Controller 是控制面入口，负责：

- 创建、查询、更新、删除 tunnel。
- 给 tunnel 签发可复用 JWT token。
- 管理 tunnel 的端口 allow list。
- 接收 Gateway 或 cluster 侧的 metering 上报。
- 校验 namespace、region、cluster、过期和删除状态。

### 2.2 Relay Gateway

Relay Gateway 是数据面组件。当前项目不实现 Gateway，但保留了 Gateway 所需的控制面接口：

- 端口策略查询：`GET /open-api-inner/v1/relay-controller/clusters/{clusterId}/tunnels/{tunnelId}/ports/{port}`
- 计量上报：`POST /open-api-inner/v1/relay-controller/clusters/{clusterId}/metering`
- 每个 tunnel 签发 `connect` 和 `host` 两个 JWT，通过 `scp` 区分权限。

Gateway port policy 接口保留 `clusterId` 是有意设计。Gateway 调用时应以自己的 cluster 作为作用域，Relay Controller 会确认 tunnel 属于这个 cluster 后才返回端口策略，避免同 region 内跨 cluster 查询策略。

### 2.3 Cluster

Cluster 是 tunnel 的部署/接入单元。每个 cluster 归属一个 region。

当前本服务判断“是否本地 cluster”的方式是查询 `cluster` 表：

- `cluster.cluster_id = clusterId`
- `cluster.region = relay.region`

如果 cluster 不属于当前 Relay Controller 实例的 region，会被视为不可访问。

### 2.4 Region

Region 是 Relay Controller 的服务边界。配置项：

```yaml
relay:
  region: region-a
```

所有 tunnel 查询、端口策略、JWT 签发、metering 都要落在当前 region 范围内。不能仅凭请求里传入的 clusterId 或数据库里查到 tunnel 就信任，必须通过 cluster-region 关系过滤。

### 2.5 Namespace

Namespace 是租户或调用方隔离维度。当前服务读取 `X-Namespace` header 作为 namespace。

接口上有两种 namespace 语义：

- 控制台/租户侧接口：`X-Namespace` 必填。
- Gateway 内部接口：不使用 namespace，但必须通过 `clusterId` 校验本地 cluster 和 tunnel 归属。

### 2.6 Tunnel

Tunnel 是核心资源。它代表一个可被 Relay Gateway 识别的转发通道元数据，但本服务不执行转发。

核心字段：

- `tunnelId`：8 位 lowercase base32 字符串，来自 40-bit tunnelCode。
- `tunnelCode`：40-bit long，作为内部更紧凑、数值型的 tunnel 标识。
- `clusterId`：绑定的 cluster。
- `namespace`：归属 namespace。
- `expiration`：过期时间，Unix seconds。
- `url`：外部访问 URL，目前格式为 `{tunnelId}-{clusterId}-{relay.domain}`。
- `type`：`bridge` 或 `env`。
- `deleted`：软删标记。

### 2.7 Tunnel Port

`tunnel_port` 是 tunnel 的端口策略子表。它表达“某个 tunnel 的某个 port 是否允许匿名发送侧访问”。

关键规则：

- 端口范围为 `1-65535`。
- `(tunnelCode, port)` 唯一。
- 未配置的端口默认拒绝。
- `allowAnonymous` 只控制发送侧访问。
- Gateway 监听侧连接仍需要 token 认证。

### 2.8 Token

Token 是 tunnel 访问凭证。tunnel create 和 detail 都会返回 `connect`、`host` 两个可复用 JWT。

关键规则：

- `jwt.connect` 用于连接 tunnel，`jwt.host` 用于承载 tunnel。
- Redis 缓存 key：`jwt:token:{tunnelId}:{scope}`。
- token 有效期取 `min(relay.jwt.token.ttl-seconds, tunnel 剩余有效期)`。
- `jwt.expiresIn` 表示当前返回 token 的剩余秒数；缓存命中时取 Redis TTL。
- tunnel expiration 更新或 tunnel 删除时会删除缓存 token。

### 2.9 Metering

Metering 是流量计量上报。调用方通常是 Gateway 或 cluster 内部组件。

上报时必须同时传入：

- `clusterId` path
- `tunnelId`
- `tunnelCode`
- `usage`

服务会校验 tunnel 属于当前 region、属于该 cluster、未过期，并且请求里的 `tunnelCode` 和数据库 tunnelCode 一致。

## 3. 技术栈

当前主要依赖：

- Java 17
- Spring Boot 3.5.16
- Spring MVC
- Jetty embedded server
- Maven
- MySQL
- Flyway
- MyBatis Plus
- Redis with Jedis client
- Nimbus JOSE JWT
- Apache Commons Codec
- MapStruct
- Lombok
- OpenAPI Generator Maven Plugin
- Swagger annotations Jakarta

重要依赖取舍：

- 使用 Jetty，排除 Tomcat starter，降低 JDK 26 下 Tomcat native 相关启动告警。
- 使用 Jedis，排除 Lettuce/Netty，降低 JDK 26 下 Netty Unsafe 相关启动告警。
- 仅保留 `swagger-annotations-jakarta`，用于编译 OpenAPI Generator 生成接口中的注解引用。

## 4. 接口统一规范

### 4.1 接口前缀

所有业务接口统一前缀：

```text
/open-api-inner/v1/relay-controller
```

静态 OpenAPI YAML 不使用该前缀：

```text
GET /openapi.yaml
```

### 4.2 返回结构

所有业务接口返回统一 `Result<T>`：

```json
{
  "error_code": "0000",
  "error_message": "success",
  "data": {}
}
```

成功 code 为 `0000`，兼容其他微服务风格。

错误字段为：

- `error_code`
- `error_message`

### 4.3 HTTP 状态码

大部分业务错误通过统一 `Result` 返回，HTTP status 通常仍是 `200`。例如参数错误、业务错误、namespace 缺失等。

### 4.4 错误码

当前错误码定义在 `ErrorCode`：

| Code | 含义 |
| --- | --- |
| `0000` | success |
| `40000` | parameter invalid |
| `40100` | unauthorized |
| `10001` | cluster not found |
| `10002` | tunnel not found |
| `10003` | tunnel id conflict |
| `10004` | tunnel expired |
| `10005` | tunnel access denied |
| `11001` | tunnel port invalid |
| `11002` | tunnel port already exists |
| `11003` | tunnel port not found |
| `11004` | tunnel port access denied |
| `30001` | jwt generate failed |
| `30002` | jwt key invalid |
| `40001` | metering report failed |
| `50000` | internal error |

## 5. API 清单与业务语义

### 5.1 Tunnel API

#### Create Tunnel

```text
POST /open-api-inner/v1/relay-controller/tunnels
```

请求要求：

- `X-Namespace` 必填。
- body 必填。
- `name` 必填。
- `clusterId` 必填。
- `type` 可选，默认为 `bridge`。
- `expiration` 可选，单位是小时，默认为 `relay.default-expiration-hours`。

业务流程：

1. 读取并校验 namespace。
2. 校验 tunnel type，默认为 `bridge`。
3. 校验 cluster 是否属于当前 region。
4. 计算过期时间：`now + expirationHours * 3600`。
5. 生成 40-bit tunnelCode。
6. 将 tunnelCode 转成 base32 tunnelId。
7. 检查 tunnelCode 和 tunnelId 在全表中未冲突。
8. 构造 tunnel URL：`{tunnelId}-{clusterId}-{relay.domain}`。
9. 插入 `tunnel` 表。
10. 返回创建后的 tunnel 元数据。

为什么冲突检查不只检查未删除 tunnel：

- tunnelCode 和 tunnelId 会出现在 token、计量、网关日志、历史排障记录中。
- 即使 tunnel 被软删，也不建议复用同一标识。
- 因此冲突检查对全表生效，保证历史 ID 不复用。

#### List Tunnels

```text
GET /open-api-inner/v1/relay-controller/tunnels?clusterId=
```

请求要求：

- `X-Namespace` 必填。
- `clusterId` 可选。

业务语义：

- 只返回当前 namespace 下的 tunnel。
- 只返回当前 region 下的 tunnel。
- 只返回 `deleted = 0` 且 `expiration > now` 的 active tunnel。
- 如果传入 `clusterId`，先校验该 cluster 属于当前 region。

#### Get Tunnel Detail

```text
GET /open-api-inner/v1/relay-controller/tunnels/{tunnelId}
```

请求要求：

- `X-Namespace` 必填。
- tunnel 必须属于当前 region。
- tunnel 必须属于该 namespace。
- tunnel 未删除、未过期。

返回字段只包含 `tunnelId`，不提供同义 `id` 字段。

#### Update Tunnel

```text
PUT /open-api-inner/v1/relay-controller/tunnels/{tunnelId}
```

请求要求：

- `X-Namespace` 必填。
- path 中的 `tunnelId` 是唯一可信来源。
- tunnel 必须属于当前 region、namespace，且未过期。

可更新字段：

- `name`
- `description`
- `cluster`
- `expiration`
- `type`

更新 expiration 时：

- expiration 入参仍是小时数。
- 新过期时间从 update time 开始计算。
- 会 evict Redis 中 `jwt:token:{tunnelId}:{scope}`，防止旧 token 有效期超过新 tunnel 生命周期。

#### Delete Tunnel

```text
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}
```

业务语义：

- 删除是软删：设置 `deleted = 1`。
- 删除时清理该 tunnel 的所有 tunnel_port 策略。
- 删除时 evict token cache。
- 删除允许处理已过期 tunnel，方便清理。

#### Delete All Tunnels

```text
DELETE /open-api-inner/v1/relay-controller/tunnels
```

业务语义：

- 删除当前 namespace、当前 region 下所有未删除 tunnel。
- 包括过期 tunnel，因为此接口用于批量清理。
- 对每个 tunnel 执行软删、token evict、port policy 删除。

### 5.2 Tunnel Detail JWT

业务语义：

- namespace 侧通过 tunnel detail 获取 JWT。
- tunnel 必须属于当前 region。
- tunnel 必须未删除、未过期。
- 必须校验 `X-Namespace` 与 tunnel namespace 归属。
- 如果 Redis 有可用缓存，直接返回缓存 token。
- 如果 Redis 无缓存或不可用，则重新签发 JWT。
- JWT 和 Redis 缓存 TTL 取 `min(relay.jwt.token.ttl-seconds, tunnel 剩余有效期)`。
- 返回的 `expiresIn` 与实际返回 token 的剩余 TTL 对齐。

JWT claims：

- `iss`：`relay.jwt.issuer`
- `exp`：token 过期时间
- `nbf`：签发时间
- `tunnelId`
- `clusterId`
- `scp`：`connect` 或 `host`

JWT header：

- `alg`：`RS256`
- `typ`：`JWT`
- `kid`：`relay.jwt.key-id`，默认 `"1"`

`key-id = 1` 当前作为单密钥场景默认 kid，后续如果做密钥轮换，可扩展为多 key id。

### 5.3 Tunnel Port API

#### Create Tunnel Port Policy

```text
POST /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
```

请求要求：

- `X-Namespace` 必填。
- tunnel 属于当前 region 和 namespace。
- tunnel 未删除、未过期。
- `port` 范围为 `1-65535`。
- `allowAnonymous` 必填。
- 同一个 `(tunnelCode, port)` 不能重复。

#### List Tunnel Port Policies

```text
GET /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
```

返回某个 tunnel 下所有 port policy，按 port 升序。

#### Get Tunnel Port Policy

```text
GET /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
```

用于 namespace 侧查询某个端口策略。

#### Update Tunnel Port Policy

```text
PUT /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
```

当前只更新 `allowAnonymous`。

#### Delete Tunnel Port Policy

```text
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports/{port}
```

物理删除指定端口策略。

#### Delete All Tunnel Port Policies

```text
DELETE /open-api-inner/v1/relay-controller/tunnels/{tunnelId}/ports
```

物理删除该 tunnel 下全部端口策略。

### 5.4 Gateway Tunnel Port Policy API

```text
GET /open-api-inner/v1/relay-controller/clusters/{clusterId}/tunnels/{tunnelId}/ports/{port}
```

这个接口给 Gateway 使用，不要求 `X-Namespace`。

业务流程：

1. 校验 path 中的 `clusterId` 是当前 region 的本地 cluster。
2. 按 `tunnelId + relay.region` 查 tunnel。
3. 校验 tunnel 未删除、未过期。
4. 校验 tunnel.clusterId 等于 path clusterId。
5. 校验 port 有配置。
6. 返回 `tunnelId`、`tunnelCode`、`clusterId`、`port`、`allowAnonymous`。

为什么不删除这个接口：

- Gateway 不一定持有 namespace。
- Gateway 的天然作用域是 cluster，而不是用户 namespace。
- path 中 clusterId 可以防止同 region 内跨 cluster 查询端口策略。
- port 是策略子资源，必须显式传入，否则无法判断访问哪个端口。

### 5.5 Metering API

```text
POST /open-api-inner/v1/relay-controller/clusters/{clusterId}/metering
```

请求：

```json
{
  "tunnelCode": 123456,
  "tunnelId": "aaaadysa",
  "usage": 1024
}
```

业务流程：

1. 校验 cluster 属于当前 region。
2. 按 `tunnelId + relay.region` 查 tunnel。
3. 校验 tunnel 属于 path clusterId。
4. 校验 tunnel 未删除、未过期。
5. 校验 request.tunnelCode 等于 tunnel.tunnelCode。
6. 插入 `metering` 表。
7. 在当前 region 范围内累加 `tunnel.bandwidth_used`。

## 6. 数据模型

### 6.1 cluster

用途：记录 cluster 与 region 的归属关系。

字段：

- `_id`
- `cluster`
- `region`
- `created_at`
- `updated_at`

约束和索引：

- `uk_cluster (cluster)`
- `idx_region (region)`

Flyway 初始数据会 seed：

```text
cluster-a -> region-a
```

### 6.2 tunnel

用途：保存 tunnel 核心元数据。

字段：

- `_id`
- `name`
- `tunnel_id`
- `tunnel_code`
- `cluster_id`
- `expiration`
- `namespace`
- `description`
- `cluster`
- `bandwidth_used`
- `url`
- `type`
- `deleted`
- `created_at`
- `updated_at`

约束和索引：

- `uk_tunnel_id (tunnel_id)`
- `uk_tunnel_code (tunnel_code)`
- `idx_namespace`
- `idx_cluster_id`
- `idx_namespace_deleted`

注意：

- `tunnel_id` 在第一版迁移中定义为 base32 tunnel id。
- 数据库保留 `_id` 自增主键；API 使用 `tunnelId` 作为对外标识。
- `tunnel_id` 和 `tunnel_code` 唯一约束不因软删释放。

### 6.3 tunnel_port

用途：保存 tunnel 端口策略。

字段：

- `_id`
- `tunnel_code`
- `port`
- `allow_anonymous`

约束和索引：

- `uk_tunnel_code_port (tunnel_code, port)`
- `idx_tunnel_code`
- `idx_port`

注意：

- tunnel_port 记录是物理删除。
- tunnel 被删除时，对应 tunnel_port 会被全部删除。

### 6.4 metering

用途：保存流量计量上报记录。

字段：

- `_id`
- `cluster_id`
- `tunnel_code`
- `tunnel_id`
- `usage_bytes`
- `reported_at`
- `created_at`

索引：

- `idx_cluster_id`
- `idx_tunnel_id`
- `idx_tunnel_code`
- `idx_reported_at`

Metering 是历史记录，不随 tunnel 软删清理。

### 6.5 Redis 数据

#### token cache

```text
jwt:token:{tunnelId}:{scope}
```

value 为 JWT 字符串。

TTL 取：

```text
min(relay.jwt.token.ttl-seconds, tunnel.expiration - now)
```

Redis 不可用时，token 签发仍然可用，只是无法复用缓存。

## 7. tunnelId 与 tunnelCode

当前设计：

- `tunnelCode` 是 40-bit long。
- `tunnelId` 是 40-bit code 的 fixed width lowercase base32 编码。
- 编码后长度为 8 个字符。
- 正则约束：`^[a-z2-7]{8}$`

生成逻辑：

- `TunnelCodeGenerator.generate()` 使用 `SecureRandom` 生成 `1` 到 `(1 << 40) - 1`。
- `TunnelCodeGenerator.toTunnelId()` 使用 `Base32Utils.encode40Bit()`。
- 创建 tunnel 时最多重试 5 次。
- 如果 5 次都冲突，抛出 `TUNNEL_ID_CONFLICT`。

为什么同时保留 tunnelId 和 tunnelCode：

- `tunnelId` 适合 URL、API path、人工查看。
- `tunnelCode` 适合 Gateway、计量、存储、二进制/数值型处理。
- 两者来自同一个 40-bit 值，不是两个独立随机源。

## 8. 生命周期规则

### 8.1 创建

创建 tunnel 会生成：

- tunnelCode
- tunnelId
- URL
- expiration
- namespace
- clusterId

默认 expiration：

```yaml
relay:
  default-expiration-hours: 72
```

请求也可以传入 expiration，单位是小时。

### 8.2 查询

List 只返回 active tunnel：

- `deleted = 0`
- `expiration > now`
- namespace 匹配
- region 匹配
- clusterId 可选匹配

Detail 要求：

- tunnel 未删除
- tunnel 未过期
- namespace 匹配
- region 匹配

### 8.3 更新

Update 要求 tunnel 未过期。过期 tunnel 不允许更新。

更新 expiration 时：

- 重新计算 Unix seconds。
- 清理 token cache。

### 8.4 删除

删除 tunnel 是软删：

```text
deleted = 1
```

删除会：

- soft delete tunnel
- evict token
- 删除 tunnel_port 策略

删除允许处理过期 tunnel，因为删除的业务目的就是清理资源。

### 8.5 过期

过期不自动物理删除。过期后的行为：

- list 不返回。
- detail 拒绝。
- update 拒绝。
- token 拒绝。
- port 操作拒绝。
- metering 拒绝。
- delete 允许。

## 9. 代码结构

### 9.1 顶层结构

```text
src/main/java/com/huawei/devbridge/relaycontroller
├── RelayControllerApplication.java
├── application
│   ├── assembler
│   └── service
├── common
│   ├── exception
│   ├── model
│   └── util
├── domain
│   ├── model
│   ├── repository
│   └── service
├── infrastructure
│   ├── config
│   ├── persistence
│   ├── redis
│   └── security
└── interfaces
    ├── controller
    ├── request
    └── response
```

### 9.2 分层职责

#### interfaces

接口层，负责 HTTP 入参出参和 Controller 实现。

Controller 不手写 request mapping，而是实现 OpenAPI Generator 生成的 API interface。

主要类：

- `TunnelController`
- `TunnelPortController`
- `MeteringController`

#### application

应用服务层，负责编排业务流程、事务、调用 domain service 和 repository。

主要类：

- `TunnelAppService`
- `TunnelPortAppService`
- `MeteringAppService`
- `LocalClusterService`

#### domain

领域层，保存核心模型、仓储接口和业务规则。

主要类：

- `TunnelDomainService`
- `TunnelPortDomainService`
- `TunnelCodeGenerator`
- `NamespaceService`
- `JwtTokenService`

#### infrastructure

基础设施层，负责 MySQL、Redis、JWT、配置、转换。

主要模块：

- `persistence`
- `redis`
- `security`
- `config`

#### common

通用能力，包括：

- 统一返回 `Result`
- 业务异常 `BizException`
- 错误码 `ErrorCode`
- 全局异常处理 `GlobalExceptionHandler`
- Base32、UUID、字符串、时间工具

## 10. 关键类职责

### 10.1 TunnelAppService

负责 tunnel 主生命周期：

- 创建 tunnel
- list active tunnel
- detail
- update
- soft delete one
- soft delete all
- tunnelCode/tunnelId 分配
- URL 构造
- expiration 计算

关键设计：

- 创建前校验 cluster 是否属于当前 region。
- list 使用 `findActiveByNamespaceAndRegion`，直接在 SQL 层过滤过期和软删。
- update 使用 `findOwnedActiveTunnel`，过期 tunnel 不允许更新。
- delete 使用 `findOwnedTunnel`，允许删除过期 tunnel。
- delete all 使用 `findByNamespaceAndRegion`，包括过期 tunnel，便于清理。

### 10.2 TunnelPortAppService

负责 tunnel port policy：

- create
- list
- detail
- update allowAnonymous
- delete one
- delete all
- Gateway port policy query

关键设计：

- namespace 侧操作必须带 `X-Namespace`。
- Gateway policy query 不要求 namespace，但要求 clusterId 是本地 cluster，且 tunnel 属于该 cluster。
- 所有 port 操作都拒绝过期 tunnel。

### 10.3 JwtTokenServiceImpl

负责 token 缓存与签发：

- 先查 Redis token cache。
- 命中则返回。
- 未命中则签发 JWT 并写入 Redis。
- Redis 失败不影响 token 生成。
- TTL 被 tunnel 剩余有效期截断。
- 缓存命中时返回 Redis 剩余 TTL，避免 `expiresIn` 高估。

### 10.4 JwtSigner

负责 JWT 签名：

- RS256
- header 带 `kid`
- claims 带 tunnel 关键业务字段

### 10.6 JwtKeyProvider

负责私钥初始化：

- 如果配置了 `relay.jwt.private-key`，解析 PKCS8 PEM。
- 如果未配置，启动时生成 ephemeral RSA 2048 key pair。

生产建议必须配置稳定 private key，否则服务重启后旧 token 无法被后续公钥体系稳定验证。

### 10.7 MeteringAppService

负责计量上报：

- 校验本地 cluster。
- 校验 tunnel 属于该 cluster。
- 校验 tunnelCode 与 tunnelId 对应。
- 写入 metering 表。
- 在当前 region 范围内累加 tunnel.bandwidth_used。

### 10.9 LocalClusterService

负责本地 cluster 判断：

- 按 `clusterId + relay.region` 查询 cluster。
- 查不到则抛 `CLUSTER_NOT_FOUND`。

### 10.10 TunnelDomainService

负责 tunnel 领域规则：

- tunnel 存在且未软删。
- namespace 归属。
- 未过期。
- cluster 匹配。

### 10.11 TunnelPortDomainService

负责 port policy 参数校验：

- port 不为空。
- port 在 `1-65535`。
- allowAnonymous 不为空。

### 10.12 PersistenceConverter

MapStruct mapper，负责 entity 与 domain model 转换：

- `ClusterEntity -> Cluster`
- `TunnelEntity <-> Tunnel`
- `TunnelPortEntity <-> TunnelPort`
- `Metering -> MeteringEntity`

## 11. OpenAPI 与接口生成

契约源文件：

```text
src/main/resources/static/openapi.yaml
```

生成目录：

```text
target/generated-sources/openapi/src/main/java
```

Maven 插件：

- `maven-antrun-plugin`：generate-sources 阶段删除旧 generated sources。
- `openapi-generator-maven-plugin`：按 YAML 生成 Spring interface。
- `build-helper-maven-plugin`：把 generated sources 加入编译 source root。

生成策略：

- `generatorName=spring`
- `interfaceOnly=true`
- `skipDefaultInterface=true`
- `useSpringBoot3=true`
- `useTags=true`
- 不生成 model tests。
- 不生成 supporting files。
- schema 映射到项目已有 request/response/result 类型。

Controller 只实现生成接口。接口 path、method、参数来源由 YAML 控制。

## 12. 配置说明

### 12.1 服务配置

默认配置文件：

```text
src/main/resources/application.yml
```

关键项：

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev
  datasource:
    url: ${DATASOURCE_URL}
    username: ${DATASOURCE_USERNAME}
    password: ${DATASOURCE_PASSWORD}
  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: true
  data:
    redis:
      client-type: jedis
      host: ${REDIS_HOST:localhost}
      port: 6379
      password: ${REDIS_PASSWORD}

relay:
  domain: myhuaweicloud.com
  region: region-a
  default-expiration-hours: 72
  jwt:
    issuer: devbridge
    key-id: "1"
    private-key:
    token:
      ttl-seconds: 86400
```

项目使用 `mysql-connector-j`，Spring Boot 根据 `DATASOURCE_URL` 自动识别驱动，不显式配置 driver class。

### 12.2 日志配置

当前日志目标：

- root 为 WARN。
- 启动完成保留一条 INFO：`Relay Controller started on port 8080`。
- 应用服务关键业务日志为 INFO。
- 全局异常处理为 WARN。
- MyBatis Plus Sequence 慢初始化 banner 被关闭。
- Flyway MySQL 版本兼容提示被压到 ERROR，以保持本地启动干净。

### 12.3 Maven 配置

`.mvn/maven.config`：

```text
--quiet
--no-transfer-progress
```

减少 Maven 输出噪音。

`.mvn/jvm.config`：

```text
--enable-final-field-mutation=ALL-UNNAMED
--sun-misc-unsafe-memory-access=allow
```

用于降低 JDK 26 下 Maven/测试相关告警。

## 13. 启动与依赖

本地启动前需要：

- MySQL
- Redis
- 数据库 `relay_controller`

数据库创建示例：

```sql
CREATE DATABASE IF NOT EXISTS relay_controller
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;
```

启动：

```bash
mvn spring-boot:run
```

启动成功后应看到：

```text
Relay Controller started on port 8080
```

OpenAPI YAML：

```text
GET http://localhost:8080/openapi.yaml
```

HTTP smoke test：

```bash
bash scripts/http-smoke-test.sh
```

## 14. 事务与一致性

使用 `@Transactional` 的主要操作：

- tunnel update
- tunnel delete
- tunnel delete all
- tunnel port create/update/delete/deleteAll
- metering report

一致性策略：

- MySQL 是元数据主存储。
- Redis token 只是缓存，失败不影响 token 签发。
- tunnel 删除时清 token cache 和 port policy。
- tunnel expiration 变更时清 token cache。
- metering 插入和当前 region 内的 bandwidth 累加在同一事务内。

## 15. 安全与边界

当前安全模型是内部服务可信调用模型，不包含完整用户认证框架。

边界规则：

- namespace 由 `X-Namespace` 提供。
- region 由服务配置固定。
- cluster 必须从数据库查出并匹配当前 region。
- token 可选 namespace 校验。
- Gateway policy 查询以 clusterId 作为 scope，不使用 namespace。

需要注意：

- `X-Namespace` 当前是可信 header，生产网关层需要确保不能被外部伪造。
- JWT private key 如果不配置，启动时会生成临时 key，不适合生产。
- token 签发接口没有实现额外调用方身份认证，默认适用于内网或上游已鉴权环境。

## 16. 测试覆盖

当前测试主要覆盖：

- base32 编码固定宽度和小写格式。
- tunnelCode 到 tunnelId 转换。
- tunnel 创建、region 校验、自定义过期小时、过期参数非法。
- tunnel 更新 expiration 后 token evict。
- tunnel list 只查本 region active tunnel。
- tunnel type enum 更新。
- tunnel 删除清理 port 和 token。
- token 缓存命中、签发、TTL 被 tunnel expiration 截断。
- metering 写入与 bandwidth 累加、跨 region cluster 拒绝。
- tunnel port create/list/update/delete/deleteAll/detail。
- gateway policy cluster 校验和端口策略返回。
- Controller 层全接口 mapping、参数和统一 Result 包装。

主要测试文件：

- `Base32UtilsTest`
- `TunnelCodeGeneratorTest`
- `TunnelAppServiceTest`
- `TunnelPortAppServiceTest`
- `JwtTokenServiceTest`
- `MeteringAppServiceTest`
- `RelayControllerApiTest`

## 17. 当前设计取舍

### 17.1 软删而非物理删除

tunnel 采用软删，原因：

- 保留 tunnelId/tunnelCode 历史唯一性。
- 避免 metering、token、Gateway 日志失去关联。
- 方便问题追溯。

tunnel_port 采用物理删除，原因：

- 它是 tunnel 的策略子表，不承担长期审计主键。
- tunnel 删除后策略无业务价值。

### 17.2 list 不返回过期 tunnel

过期 tunnel 不应再作为可用资源展示，所以 list 直接在 SQL 层过滤 `expiration > now`。

### 17.3 删除允许处理过期 tunnel

删除本身是清理动作，因此过期 tunnel 仍允许删除。

### 17.4 token TTL 与 tunnel 生命周期绑定

token 不能超过 tunnel 的剩余有效期，否则 tunnel 过期后 token 仍可用会破坏业务边界。

### 17.5 Gateway policy 保留 clusterId

虽然 tunnel 能查到 clusterId，但 Gateway 请求 path 中保留 clusterId 是为了表达调用者 scope，并显式校验 tunnel 是否属于该 cluster。

### 17.6 响应不暴露数据库 id

`tunnelId` 和 `(tunnelId, port)` 是对外稳定标识。响应不额外提供数据库 `_id` 字段，避免调用方绑定内部主键。

## 18. 代码阅读路径建议

建议按以下顺序读代码：

1. `README.md`
2. `src/main/resources/static/openapi.yaml`
3. `interfaces/controller/*`
4. `application/service/TunnelAppService.java`
5. `application/service/TunnelPortAppService.java`
6. `domain/service/*`
7. `domain/repository/*`
8. `infrastructure/persistence/repository/*`
9. `src/main/resources/mapper/TunnelMapper.xml`
10. `infrastructure/security/*`
11. `infrastructure/redis/*`
12. `src/main/resources/db/migration/*`
13. `src/test/java/com/huawei/devbridge/relaycontroller/*Test.java`

## 19. 后续可优化点

这些不是当前必须项，但后续可以评估：

1. HTTP 状态码是否继续保持业务错误 200，还是改为更标准的 4xx/5xx。
2. 是否增加 tunnel 自动过期清理任务。
3. 是否增加 metering 聚合表，避免长期只存明细。
4. 是否支持多 JWT key 和 kid 轮换。
5. 是否需要为 `cluster` 增加管理接口，或者由外部系统同步。
6. 是否为 Gateway policy API 增加调用来源校验。
7. 是否将 `expiration` 响应字段重命名为 `expiresAt`，减少“小时入参 / 秒响应”的理解成本。
8. 是否增加集成测试，覆盖真实 MySQL + Redis + Flyway 场景。
