# Relay Controller US / Story

生成日期：2026-07-09

本文档面向测试、SE、控制台、Relay Gateway 以及其他微服务同事，用于需求评审、接口对齐、联调准备和测试设计。

Relay Controller 是 DevBridge / Relay Tunnel 系统的控制面服务。它负责管理 tunnel 元数据、端口策略、连接凭证、计量上报、region/grid 归属和 namespace 配额，不负责真实流量转发。

## 1 价值描述

### 作为

作为 DevBridge / Relay Tunnel 系统中的控制台、Relay Gateway、region 内部平台服务和运维侧使用方。

### 我要

通过一组统一、稳定、可校验的内部 API 完成 tunnel 生命周期管理、端口访问策略管理、连接凭证获取和流量计量上报。

### 从而

- 控制台和内部服务可以按 namespace 管理 tunnel。
- Relay Gateway 可以按 grid 查询端口策略并上报用量。
- 一个 Relay Controller 实例只管理本 region 的 grid 和 tunnel，避免跨 region 误操作。
- tunnel 过期、删除、配额、流控、TLS 认证等规则由服务端统一执行。
- 其他微服务只需要按 OpenAPI 契约对接，不需要理解内部实现。

### 现状

- 已提供 tunnel、tunnel port、gateway policy、metering 等核心接口。
- 接口统一前缀为 `/open-api-inner/v1/relay-controller`。
- OpenAPI YAML 是接口契约源，Controller 基于编译生成接口实现。
- 数据库使用 MySQL，表结构由 Flyway 初始化和演进。
- Redis 用于缓存 tunnel detail 中返回的 JWT。
- 支持 mTLS profile，服务端可要求客户端证书。
- 支持 namespace 维度 active tunnel 配额和基础 API 流控。

### 要求

- 控制台侧接口必须传 `X-Namespace`。
- Gateway 侧接口不传 namespace，以 `gridName` 作为作用域入口。
- Relay Controller 只信任配置 region 下的 grid。
- tunnel 创建时必须绑定本 region 下存在的 grid。
- 每个 namespace 默认最多 10 个 active tunnel。
- active tunnel 指未删除且未过期的 tunnel。
- tunnel 默认 72 小时过期，创建和更新时可按小时传入。
- 删除 tunnel 使用软删除，历史数据和计量关系保留。
- tunnel detail 返回 JWT，不再提供独立 token 接口。
- 第一版不实现数据面代理、复杂权限模型、独立证书获取接口、node 同步接口和 stats 接口。

## 2 功能描述

### 2.1 功能说明

#### 2.1.1 Tunnel 生命周期

Relay Controller 提供 tunnel 的创建、查询、更新和删除能力。

创建 tunnel 时，调用方需要提供：

| 字段 | 说明 |
| --- | --- |
| `gridName` | tunnel 绑定的 grid，必须属于当前 region |
| `name` | tunnel 名称 |
| `description` | tunnel 描述，可选 |
| `cluster` | 业务侧集群标识，可选 |
| `expiration` | 过期时间，单位小时，可选，默认 72 小时 |
| `type` | tunnel 类型，可选，默认 `bridge` |

创建成功后返回：

| 字段 | 说明 |
| --- | --- |
| `tunnelId` | 对外使用的 tunnel 标识 |
| `tunnelCode` | 内部数值标识 |
| `gridName` | 绑定 grid |
| `expiration` | 过期时间 |
| `url` | tunnel 访问域名 |
| `type` | tunnel 类型 |

tunnel URL 规则：

```text
{tunnelId}-{gridName}-{relay.domain}
```

例如：

```text
abc123xy-grid-a-myhuaweicloud.com
```

查询规则：

- list 只返回 active tunnel。
- detail 只允许查询未删除、未过期、属于当前 namespace 的 tunnel。
- detail 同时返回 JWT。

删除规则：

- 删除单个 tunnel：`DELETE /tunnels/{tunnelId}`。
- 删除 namespace 下全部 tunnel：`DELETE /tunnels`。
- 删除 tunnel 后，其端口策略一并删除。
- 已删除 tunnel 不再出现在 list 中。

#### 2.1.2 Tunnel Detail 与 JWT

tunnel detail 返回 tunnel 元数据和 `jwt` 信息。

JWT 用于后续连接侧或 gateway 侧识别 tunnel 权限。JWT 的有效期受两项共同限制：

- 系统配置的 token TTL。
- tunnel 剩余有效期。

最终 JWT 有效期取两者较小值。因此 tunnel 即将过期时，即使系统 token TTL 较长，返回 token 也不会超过 tunnel 生命周期。

当 tunnel 更新过期时间或被删除时，服务端会清理对应 JWT 缓存，避免旧 token 继续按旧生命周期使用。

#### 2.1.3 Tunnel Port 策略

Tunnel port 是 tunnel 的子资源，用于定义某个端口的访问策略。

支持能力：

- 创建端口策略。
- 查询端口策略列表。
- 查询单个端口策略。
- 更新端口策略。
- 删除单个端口策略。
- 删除 tunnel 下全部端口策略。

核心规则：

- 端口范围为 `1-65535`。
- 同一个 tunnel 下同一个 port 只能配置一次。
- 未配置的端口默认拒绝。
- tunnel 已删除或已过期时，不允许继续管理端口策略。

#### 2.1.4 Gateway Port Policy

Gateway 通过接口查询某个 grid、tunnel、port 是否允许访问。

服务端校验：

- grid 属于当前 region。
- tunnel 属于该 grid。
- tunnel 未删除、未过期。
- port 策略存在。

该接口不要求 `X-Namespace`，因为 Gateway 调用时以 `gridName` 和 tunnel 关系作为业务边界。

#### 2.1.5 Metering

Gateway 或 grid 侧向 Relay Controller 上报 tunnel 流量。

服务端校验：

- grid 属于当前 region。
- tunnel 存在并属于该 grid。
- tunnel 未删除、未过期。
- 请求中的 `tunnelCode` 与服务端记录一致。

上报成功后：

- 写入 metering 明细。
- 累加 tunnel 已使用流量。

#### 2.1.6 安全与接入控制

安全控制包括：

- `X-Namespace`：控制台侧资源隔离。
- region/grid 校验：避免跨 region 操作。
- active tunnel 配额：限制 namespace 资源占用。
- API rate limit：避免单 namespace 或单 IP 过量调用。
- mTLS：服务端可要求客户端证书。
- JWT：tunnel detail 返回短期连接凭证。

### 2.2 约束与依赖

#### 2.2.1 业务约束

| 约束 | 规则 |
| --- | --- |
| region 归属 | 一个 Relay Controller 实例只管理一个 configured region |
| grid 可信性 | 只能使用当前 region 下的 grid |
| namespace | 控制台侧接口必填 `X-Namespace` |
| active tunnel | `deleted = 0` 且 `expiration > now` |
| tunnel 配额 | 每个 namespace 默认最多 10 个 active tunnel |
| tunnel 删除 | 软删除，不物理删除 tunnel 历史 |
| tunnel list | 只返回 active tunnel |
| tunnel detail | 只返回 active tunnel，并带 JWT |
| port 策略 | 只允许管理 active tunnel 的 port |
| metering | 只接受当前 region、当前 grid、未过期 tunnel 的上报 |

#### 2.2.2 外部依赖

| 依赖 | 用途 |
| --- | --- |
| MySQL | 保存 grid、tunnel、port policy、metering |
| Flyway | 初始化和演进数据库表 |
| Redis | 缓存 JWT，Redis 不可用时可重新签发 |
| mTLS 证书 | 开启 `mtls` profile 时用于客户端认证 |
| JWT 私钥 | 生产环境用于签发 JWT |
| OpenAPI YAML | 微服务对接和接口生成的契约源 |

#### 2.2.3 关键配置

| 配置 | 默认/说明 |
| --- | --- |
| `relay.domain` | 默认建议为 `myhuaweicloud.com` |
| `relay.region` | 当前实例负责的 region |
| `relay.default-expiration-hours` | 默认 72 小时 |
| `relay.tunnel.max-per-namespace` | 默认 10 |
| `relay.rate-limit.requests-per-minute` | 默认 120 |
| `relay.jwt.token.ttl-seconds` | JWT 最大 TTL |
| `spring.profiles.active=mtls` | 开启 HTTPS 双向认证 |

## 3 实现设计

### 3.1 总结设计描述

整体设计以业务边界清晰、接口契约稳定、第一版复杂度可控为目标。

核心原则：

- OpenAPI YAML 作为接口契约源。
- Controller 只做入参承接和结果返回。
- 应用服务负责业务流程编排。
- 领域规则集中处理 tunnel、port、namespace、grid、过期和配额校验。
- 数据库存储使用 snake_case 字段，Java 对象使用 camelCase。
- 删除采用软删除，减少历史数据和计量数据割裂。
- 第一版优先保证业务闭环，不提前引入复杂权限系统和数据面能力。

### 3.1.1 职责边界

| 模块 | 职责 |
| --- | --- |
| Controller | 实现 OpenAPI 生成接口，统一响应格式 |
| Tunnel 应用服务 | tunnel 创建、查询、更新、删除、JWT 返回 |
| Port 应用服务 | tunnel port 策略管理 |
| Metering 应用服务 | 计量上报校验、明细写入、用量累加 |
| Grid 校验 | 确认 grid 属于当前 region |
| JWT 能力 | 签发和缓存 tunnel 连接凭证 |
| 数据访问 | 读写 MySQL 表 |
| API 流控 | 对内部 API 做基础限流 |

### 3.2 业务流程

#### 3.2.1 创建 tunnel

1. 调用方传入 `X-Namespace` 和创建参数。
2. 服务端校验 namespace。
3. 服务端校验 grid 是否属于当前 region。
4. 服务端统计当前 namespace 的 active tunnel 数量。
5. 如果达到配额，拒绝创建。
6. 生成 tunnel 标识、过期时间和访问 URL。
7. 保存 tunnel。
8. 返回 tunnel 基础信息。

#### 3.2.2 查询 tunnel list

1. 校验 namespace。
2. 可选按 `gridName` 过滤。
3. 只查询当前 region、当前 namespace、未删除、未过期的 tunnel。
4. 返回列表。

#### 3.2.3 查询 tunnel detail

1. 校验 namespace。
2. 查询 tunnel。
3. 校验 tunnel 属于当前 namespace，且未删除、未过期。
4. 获取或签发 JWT。
5. 返回 tunnel detail 和 JWT。

#### 3.2.4 更新 tunnel

1. 校验 namespace。
2. 查询并校验 tunnel 归属。
3. 更新名称、描述、cluster、type、过期时间等可变字段。
4. 如果过期时间变化，清理 JWT 缓存。
5. 返回更新结果。

#### 3.2.5 删除 tunnel

1. 校验 namespace。
2. 查询并校验 tunnel 归属。
3. 软删除 tunnel。
4. 删除该 tunnel 的端口策略。
5. 清理 JWT 缓存。
6. 返回删除结果。

#### 3.2.6 管理 tunnel port

1. 校验 namespace。
2. 查询并校验 tunnel 归属和状态。
3. 校验 port 范围。
4. 按动作创建、查询、更新或删除端口策略。
5. 返回操作结果。

#### 3.2.7 Gateway 查询 port policy

1. Gateway 传入 `gridName`、`tunnelId`、`port`。
2. 服务端校验 grid 属于当前 region。
3. 服务端校验 tunnel 属于该 grid 且未过期。
4. 服务端查询 port 策略。
5. 返回是否允许匿名访问。

#### 3.2.8 Metering 上报

1. Gateway 传入 `gridName` 和计量数据。
2. 服务端校验 grid、tunnel、tunnelCode。
3. 写入计量明细。
4. 累加 tunnel 使用流量。
5. 返回 accepted。

### 3.3 关键算法介绍

#### 3.3.1 Tunnel 标识

- `tunnelCode` 是 40-bit long。
- `tunnelId` 是该 40-bit 值的 8 位 lowercase base32 表达。
- 创建时会检查 `tunnelCode` 和 `tunnelId` 是否唯一。
- 该规则保证对外 ID 短、稳定，内部仍可使用数值标识关联 port 和 metering。

#### 3.3.2 Active Tunnel 配额

active tunnel 口径：

```text
未删除 AND 未过期
```

创建 tunnel 前按 namespace 和 region 统计 active tunnel 数量。达到上限时拒绝创建。

当前第一版使用单实例内的并发保护，避免同一 namespace 在瞬时并发下突破配额。如果未来同一个 region 部署多实例，需要补充数据库锁或 Redis 分布式锁。

#### 3.3.3 JWT 生命周期

JWT 有效期不超过 tunnel 剩余有效期。

当 tunnel 被删除或过期时间发生变化时，服务端清理 JWT 缓存。Redis 不可用时，接口仍可重新签发 token，不阻断 tunnel detail 查询。

#### 3.3.4 API 流控

默认按以下优先级确定流控对象：

1. `X-Namespace`
2. 客户端 IP

超过默认每分钟 120 次时返回 429。

### 3.4 关键代码

本节只列评审和问题定位需要关注的关键模块，不展开代码实现。

| 能力 | 关注模块 |
| --- | --- |
| 接口契约 | `src/main/resources/static/openapi.yaml` |
| tunnel 主流程 | Tunnel application service |
| port 主流程 | Tunnel port application service |
| metering 主流程 | Metering application service |
| region/grid 校验 | Local grid service |
| tunnel 规则 | Tunnel domain service |
| port 规则 | Tunnel port domain service |
| JWT | JWT signer and cache |
| 数据库迁移 | `src/main/resources/db/migration` |
| 本地联调 | `relay-controller.http`、HTTP smoke script |

### 3.5 接口定义及变更

接口统一前缀：

```text
/open-api-inner/v1/relay-controller
```

OpenAPI 契约源：

```text
src/main/resources/static/openapi.yaml
```

#### 3.5.1 Tunnel API

| Method | Path | 说明 | Namespace |
| --- | --- | --- | --- |
| POST | `/tunnels` | 创建 tunnel | 必填 |
| GET | `/tunnels` | 查询 active tunnel list | 必填 |
| DELETE | `/tunnels` | 删除 namespace 下全部 tunnel | 必填 |
| GET | `/tunnels/{tunnelId}` | 查询 tunnel detail 和 JWT | 必填 |
| PUT | `/tunnels/{tunnelId}` | 更新 tunnel | 必填 |
| DELETE | `/tunnels/{tunnelId}` | 删除 tunnel | 必填 |

#### 3.5.2 Tunnel Port API

| Method | Path | 说明 | Namespace |
| --- | --- | --- | --- |
| POST | `/tunnels/{tunnelId}/ports` | 创建端口策略 | 必填 |
| GET | `/tunnels/{tunnelId}/ports` | 查询端口策略列表 | 必填 |
| DELETE | `/tunnels/{tunnelId}/ports` | 删除全部端口策略 | 必填 |
| GET | `/tunnels/{tunnelId}/ports/{port}` | 查询单个端口策略 | 必填 |
| PUT | `/tunnels/{tunnelId}/ports/{port}` | 更新端口策略 | 必填 |
| DELETE | `/tunnels/{tunnelId}/ports/{port}` | 删除端口策略 | 必填 |

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
| `0000` | 成功 |
| `40000` | 参数错误 |
| `40100` | 未认证或无权限 |
| `10001` | grid 不存在或不属于当前 region |
| `10002` | tunnel 不存在 |
| `10004` | tunnel 已过期 |
| `10005` | tunnel 不属于当前 namespace |
| `10006` | active tunnel 超出配额 |
| `11001` | port 非法 |
| `11002` | port 已存在 |
| `11003` | port 不存在 |
| `30001` | JWT 生成失败 |
| `40001` | metering 上报失败 |
| `42900` | 请求被限流 |
| `50000` | 系统内部错误 |

#### 3.5.5 已收敛接口

当前第一版已删除或合并以下接口：

- 删除独立 token 接口，JWT 放入 tunnel detail。
- 删除 stats 接口。
- 删除 node 同步接口。
- 删除一次性 token 接口。
- 删除证书获取接口。
- `tunnel` 和 `port` 路径统一使用复数：`tunnels`、`ports`。
- delete all 使用集合资源：`DELETE /tunnels`、`DELETE /tunnels/{tunnelId}/ports`。

### 3.6 数据库表设计及数据割接设计

#### 3.6.1 表设计

| 表 | 用途 | 关键字段 |
| --- | --- | --- |
| `grid` | 记录 grid 与 region 归属 | `grid`、`region` |
| `tunnel` | 记录 tunnel 元数据和生命周期 | `tunnel_id`、`tunnel_code`、`grid_name`、`namespace`、`expiration`、`deleted`、`url`、`type` |
| `tunnel_port` | 记录 tunnel 端口策略 | `tunnel_code`、`port`、`allow_anonymous` |
| `metering` | 记录流量上报明细 | `grid_name`、`tunnel_id`、`tunnel_code`、`usage_bytes`、`reported_at` |

关键唯一性：

- `grid.grid` 唯一。
- `tunnel.tunnel_id` 唯一。
- `tunnel.tunnel_code` 唯一。
- `tunnel_port` 中 `tunnel_code + port` 唯一。

#### 3.6.2 Flyway

Flyway 用于初始化和演进数据库结构。

当前迁移：

- `V1__init_schema.sql`：创建基础表。
- `V2__clarify_tunnel_id_comments.sql`：明确 `tunnel_id` 的 base32 语义。

建议：

- 新环境直接由 Flyway 建表。
- 已有库接入时谨慎使用 baseline。
- 已发布 migration 不做原地修改，后续变更新增版本脚本。
- 生产环境保留 `validate-on-migrate=true`，防止脚本被误改。

#### 3.6.3 数据割接设计

当前第一版按新库新表建设，默认无历史数据割接。

如未来接入已有数据，需要重点校验：

- grid 是否都归属正确 region。
- tunnel 是否有唯一的 `tunnel_id` 和 `tunnel_code`。
- `tunnel_id` 是否符合 base32 语义。
- 已删除 tunnel 是否设置 `deleted = 1`。
- active tunnel 数量是否超过 namespace 配额。
- port 策略是否能按 `tunnel_code + port` 唯一映射。
- metering 明细是否保留原始上报时间。

## 5 开发者测试

### 5.1 测试建议

#### 5.1.1 联调前准备

联调环境需要准备：

- MySQL 数据库。
- Redis。
- Relay Controller 配置文件。
- 当前 region 的 grid 初始化数据。
- 如启用 mTLS，需要服务端证书、服务端 truststore 和客户端证书。
- 如启用生产 JWT，需要配置私钥。

#### 5.1.2 主流程测试

建议按以下顺序验证：

1. 获取 OpenAPI YAML。
2. 创建 tunnel。
3. 查询 tunnel list。
4. 查询 tunnel detail，确认返回 JWT。
5. 创建 tunnel port。
6. 查询 tunnel port list 和 detail。
7. Gateway 查询 port policy。
8. Gateway 上报 metering。
9. 更新 tunnel 过期时间。
10. 删除单个 port。
11. 删除全部 ports。
12. 删除单个 tunnel。
13. 删除 namespace 下全部 tunnels。

#### 5.1.3 负向测试

建议覆盖：

- 不传 `X-Namespace`。
- 使用不存在的 grid。
- 使用其他 region 的 grid。
- 创建第 11 个 active tunnel。
- 使用非法 port：0、70000、空值。
- 查询已删除 tunnel。
- 查询已过期 tunnel。
- metering 中 `tunnelCode` 与 tunnel 不匹配。
- Gateway 查询未配置 port。
- 高频请求触发 429。
- mTLS 场景下不带客户端证书访问。

#### 5.1.4 验收口径

| 场景 | 期望 |
| --- | --- |
| 创建 tunnel | 返回 tunnelId、tunnelCode、url、expiration |
| list tunnel | 只返回当前 namespace 的 active tunnel |
| detail tunnel | 返回 tunnel 元数据和 JWT |
| 删除 tunnel | list 不再出现，port 策略不可继续查询 |
| 过期 tunnel | detail、port、metering 均拒绝 |
| 配额超限 | 第 11 个 active tunnel 创建失败 |
| Gateway policy | 只允许当前 region、当前 grid、已配置 port |
| Metering | 明细写入，tunnel 用量累加 |
| mTLS | 无客户端证书时握手失败 |

### 5.2 单元测试

当前单元测试重点覆盖：

| 测试方向 | 覆盖内容 |
| --- | --- |
| tunnel 标识 | 40-bit code 和 base32 tunnelId |
| tunnel 创建 | 默认过期时间、URL、grid 校验、配额 |
| tunnel 查询 | namespace 隔离、active 过滤、detail JWT |
| tunnel 更新 | 可变字段更新、过期时间更新、JWT 缓存清理 |
| tunnel 删除 | 软删除、port 清理、JWT 缓存清理 |
| port 策略 | 端口范围、重复创建、查询、更新、删除 |
| metering | grid/tunnel 校验、tunnelCode 校验、用量累加 |
| JWT | TTL、缓存命中、缓存失效 |
| rate limit | namespace/IP 维度限流 |
| Controller | 统一响应和参数校验 |

开发提交前建议执行：

```bash
mvn test
```

如果改动 OpenAPI YAML，需要额外确认：

- 生成接口可以正常编译。
- Controller 实现与生成接口一致。
- 其他微服务依赖的 path、字段名、错误码没有非预期变化。

如果改动数据库，需要额外确认：

- 新增 Flyway migration。
- 不修改已经发布的 migration。
- Java 字段和数据库字段映射正确。
- 已有环境迁移路径明确。
