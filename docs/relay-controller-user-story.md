# Relay Controller US / Story

生成日期：2026-07-09

本文档面向测试、SE、控制台、Relay Gateway 和其他微服务同事，用于第一版需求评审、接口对齐、联调准备和测试设计。

Relay Controller 是 DevBridge / Relay Tunnel 系统的控制面服务。它负责 tunnel 元数据、端口策略、连接凭证、计量上报、region/cluster 归属和 namespace 配额，不负责真实流量转发。

## 1 价值描述

### 作为

作为 DevBridge / Relay Tunnel 系统中的控制台、Relay Gateway、region 内部平台服务和运维侧使用方。

### 我要

通过统一、稳定、可校验的内部 API 管理 tunnel 生命周期、端口访问策略、连接凭证和流量计量。

### 从而

- 控制台和内部服务可以按 namespace 管理 tunnel。
- Relay Gateway 可以按 cluster 查询端口策略并上报用量。
- 一个 Relay Controller 实例只管理本 region 的 cluster 和 tunnel，避免跨 region 误操作。
- tunnel 过期、删除、配额、流控、TLS 认证等规则由服务端统一执行。
- 其他微服务按 OpenAPI 契约对接即可，不需要理解内部实现。

### 现状

- 第一版已覆盖 tunnel、tunnel port、gateway policy、metering 等控制面核心能力。
- 接口统一前缀为 `/open-api-inner/v1/relay-controller`。
- OpenAPI YAML 是接口契约源。
- MySQL 保存业务数据，Flyway 管理表结构。
- Redis 用于缓存 tunnel detail 返回的 JWT。
- 支持 mTLS、namespace 维度配额和基础 API 流控。

### 要求

- 控制台侧接口必须传 `X-Namespace`。
- Gateway 侧接口以 `clusterId` 作为作用域入口，不传 namespace。
- Relay Controller 只信任配置 region 下的 cluster。
- tunnel 创建时必须绑定本 region 下存在的 cluster。
- 每个 namespace 默认最多 10 个 active tunnel。
- active tunnel 指未删除且未过期的 tunnel。
- tunnel 默认 72 小时过期，可按小时传入。
- tunnel 删除采用软删除，历史数据和计量关系保留。
- tunnel create 和 detail 返回 tunnel 元数据，以及 `connect`、`host` 两个 JWT。
- 第一版范围聚焦控制面业务闭环，不覆盖数据面代理、复杂权限、证书管理、node 同步和 stats 统计。

## 2 功能描述

### 2.1 功能说明

#### 2.1.1 Tunnel 生命周期

Relay Controller 提供 tunnel 创建、查询、更新和删除能力。

创建 tunnel 时，调用方指定所属 cluster 和基础业务信息。服务端校验 cluster 归属、namespace 配额和过期时间，并生成 tunnel 标识和访问域名。

查询 tunnel 时：

- list 只返回当前 namespace 下的 active tunnel。
- detail 只返回当前 namespace 下未删除、未过期的 tunnel。
- detail 同时返回 `connect`、`host` 两个 JWT。

删除 tunnel 时：

- 支持删除单个 tunnel。
- 支持删除 namespace 下全部 tunnel。
- 删除后 tunnel 不再出现在 list 中。
- tunnel 下的端口策略一并清理。

#### 2.1.2 Tunnel Detail 与 JWT

Tunnel detail 用于返回 tunnel 元数据，以及按 `scp` 分离的 `connect`、`host` JWT。

JWT claims 只包含 `iss`、`exp`、`nbf`、`tunnelId`、`clusterId`、`scp`。有效期受 tunnel 剩余有效期约束，不会超过 tunnel 生命周期；tunnel 被删除或过期时间变化时，服务端会清理两个 scope 的缓存。

#### 2.1.3 Tunnel Port 策略

Tunnel port 是 tunnel 的子资源，用于定义端口访问策略。

支持创建、查询、更新、删除单个端口策略，也支持删除 tunnel 下全部端口策略。

核心规则：

- 端口范围为 `1-65535`。
- 同一个 tunnel 下同一个 port 只能配置一次。
- 未配置端口默认拒绝。
- tunnel 已删除或已过期时，不允许继续管理端口策略。

#### 2.1.4 Gateway Port Policy

Gateway 按 cluster、tunnel、port 查询是否允许访问。

服务端校验 cluster 属于当前 region，tunnel 属于该 cluster，tunnel 未删除未过期，并且对应 port 策略存在。

#### 2.1.5 Metering

Gateway 或 cluster 侧向 Relay Controller 上报 tunnel 流量。

服务端校验 cluster、tunnel、tunnelCode 的一致性。校验通过后写入计量明细，并累加 tunnel 已使用流量。

#### 2.1.6 安全与接入控制

安全控制包括：

- `X-Namespace`：控制台侧资源隔离。
- region/cluster 校验：避免跨 region 操作。
- active tunnel 配额：限制 namespace 资源占用。
- API rate limit：限制异常高频调用。
- mTLS：服务端可要求客户端证书。
- JWT：tunnel detail 返回短期连接凭证。

### 2.2 约束与依赖

#### 2.2.1 业务约束

| 约束 | 规则 |
| --- | --- |
| region | 一个 Relay Controller 实例只管理一个 configured region |
| cluster | 只能使用当前 region 下的 cluster |
| namespace | 控制台侧接口必填 `X-Namespace` |
| active tunnel | 未删除且未过期 |
| tunnel 配额 | 每个 namespace 默认最多 10 个 active tunnel |
| tunnel 删除 | 软删除，不物理删除历史 |
| tunnel list | 只返回 active tunnel |
| tunnel detail | 返回 active tunnel 和 JWT |
| port 策略 | 只允许管理 active tunnel 的 port |
| metering | 只接受当前 region、当前 cluster、未过期 tunnel 的上报 |

#### 2.2.2 外部依赖

| 依赖 | 用途 |
| --- | --- |
| MySQL | 保存 cluster、tunnel、port policy、metering |
| Flyway | 初始化和演进数据库表 |
| Redis | 缓存 JWT |
| mTLS 证书 | 客户端认证 |
| JWT 私钥 | 生产环境签发 JWT |
| OpenAPI YAML | 接口契约源 |

## 3 实现设计

### 3.1 总结设计描述

第一版设计目标是边界清晰、接口稳定、业务闭环完整。

核心原则：

- OpenAPI YAML 作为接口契约源。
- Controller 承接请求和统一响应。
- 应用服务编排 tunnel、port、metering 主流程。
- 领域规则集中处理 namespace、region/cluster、过期、配额和端口校验。
- 数据库存储使用 snake_case 字段，Java 对象使用 camelCase。
- 删除采用软删除，避免历史计量和 tunnel 关系割裂。

### 3.1.1 职责边界

| 模块 | 职责 |
| --- | --- |
| Controller | 实现 OpenAPI 生成接口，返回统一响应 |
| Tunnel 服务 | tunnel 创建、查询、更新、删除、JWT 返回 |
| Port 服务 | tunnel port 策略管理 |
| Metering 服务 | 计量上报校验、写入和用量累加 |
| Cluster 校验 | 确认 cluster 属于当前 region |
| JWT 能力 | 签发和缓存连接凭证 |
| 数据访问 | 读写 MySQL |
| API 流控 | 对内部 API 做基础限流 |

### 3.2 业务流程

#### 3.2.1 Tunnel 主流程

创建 tunnel 时，服务端校验 namespace、cluster、配额和过期时间，生成 tunnel 标识和访问域名后保存。

查询 tunnel 时，服务端按 namespace、region、删除状态和过期状态过滤。detail 会额外返回 JWT。

更新 tunnel 时，服务端只允许更新基础信息和过期时间。过期时间变化时清理 JWT 缓存。

删除 tunnel 时，服务端软删除 tunnel，清理端口策略和 JWT 缓存。

#### 3.2.2 Port 主流程

端口策略始终挂在 active tunnel 下。服务端校验 tunnel 归属、状态和端口范围后，再执行创建、查询、更新或删除。

#### 3.2.3 Gateway 主流程

Gateway 查询 port policy 时，服务端以 `clusterId` 为入口校验 region 归属，再校验 tunnel 与 port 策略。

Gateway 上报 metering 时，服务端校验 cluster、tunnel、tunnelCode 一致后写入明细并累加用量。

### 3.3 关键算法介绍

#### 3.3.1 Tunnel 标识

- `tunnelCode` 是 40-bit long。
- `tunnelId` 是该 40-bit 值的 8 位 lowercase base32 表达。
- 创建时检查 `tunnelCode` 和 `tunnelId` 唯一性。

#### 3.3.2 配额与过期

active tunnel 口径为未删除且未过期。创建前按 namespace 和 region 统计 active tunnel 数量，达到上限时拒绝创建。

#### 3.3.3 JWT 生命周期

JWT 有效期不超过 tunnel 剩余有效期。Redis 用于缓存 JWT，Redis 不可用时不阻断 detail 查询。

#### 3.3.4 API 流控

默认按 `X-Namespace` 限流；没有 namespace 时按客户端 IP 限流。超限返回 429。

### 3.4 关键代码

本节只列评审和问题定位需要关注的模块，不展开实现细节。

| 能力 | 关注模块 |
| --- | --- |
| 接口契约 | `src/main/resources/static/openapi.yaml` |
| tunnel / port / metering 主流程 | application service |
| region/cluster 与领域规则 | domain service |
| JWT | JWT signer and cache |
| 数据库迁移 | `src/main/resources/db/migration` |
| 本地联调 | `relay-controller.http`、HTTP smoke script |

### 3.5 接口定义

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
| GET | `/clusters/{clusterId}/tunnels/{tunnelId}/ports/{port}` | Gateway 查询端口策略 | 不需要 |
| POST | `/clusters/{clusterId}/metering` | Gateway/cluster 上报计量 | 不需要 |

#### 3.5.4 关键字段口径

| 字段 | 说明 |
| --- | --- |
| `clusterId` | tunnel 绑定 cluster，必须属于当前 region |
| `expiration` | tunnel 过期时间，单位小时，默认 72 小时 |
| `type` | tunnel 类型，默认 `bridge` |
| `tunnelId` | 对外使用的 tunnel 标识 |
| `tunnelCode` | 内部数值标识 |
| `url` | `{tunnelId}-{clusterId}-{relay.domain}` |
| `jwt` | tunnel detail 返回的连接凭证 |

#### 3.5.5 统一响应

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
| `10001` | cluster 不存在或不属于当前 region |
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

#### 3.5.6 第一版 API 口径

- 核心接口覆盖 tunnel、tunnel port、gateway policy、metering。
- tunnel detail 返回连接 JWT。
- 资源路径统一使用复数：`tunnels`、`ports`。
- delete all 使用集合资源：`DELETE /tunnels`、`DELETE /tunnels/{tunnelId}/ports`。
- stats、node 同步、证书管理、数据面代理不在第一版 API 范围内。

### 3.6 数据库表设计及数据割接设计

#### 3.6.1 表设计

| 表 | 用途 | 关键字段 |
| --- | --- | --- |
| `cluster` | 记录 cluster 与 region 归属 | `cluster`、`region` |
| `tunnel` | 记录 tunnel 元数据和生命周期 | `tunnel_id`、`tunnel_code`、`cluster_id`、`namespace`、`expiration`、`deleted`、`url`、`type` |
| `tunnel_port` | 记录 tunnel 端口策略 | `tunnel_code`、`port`、`allow_anonymous` |
| `metering` | 记录流量上报明细 | `cluster_id`、`tunnel_id`、`tunnel_code`、`usage_bytes`、`reported_at` |

关键唯一性：

- `cluster` 唯一。
- `tunnel_id` 唯一。
- `tunnel_code` 唯一。
- `tunnel_code + port` 唯一。

#### 3.6.2 数据割接设计

第一版按新库新表建设，默认无历史数据割接。

如未来接入已有数据，需要重点校验：

- cluster 是否归属正确 region。
- tunnel 是否有唯一的 `tunnel_id` 和 `tunnel_code`。
- `tunnel_id` 是否符合 base32 语义。
- active tunnel 数量是否超过 namespace 配额。
- port 策略是否能按 `tunnel_code + port` 唯一映射。

## 5 开发者测试

### 5.1 测试建议

#### 5.1.1 联调前准备

联调环境需要准备 MySQL、Redis、Relay Controller 配置、当前 region 的 cluster 初始化数据。如启用 mTLS，需要准备服务端证书、服务端 truststore 和客户端证书；如使用生产 JWT，需要配置私钥。

#### 5.1.2 主流程测试

建议按以下顺序验证：

1. 获取 OpenAPI YAML。
2. 创建 tunnel。
3. 查询 tunnel list。
4. 查询 tunnel detail，确认返回 JWT。
5. 创建并查询 tunnel port。
6. Gateway 查询 port policy。
7. Gateway 上报 metering。
8. 更新 tunnel 过期时间。
9. 删除 port。
10. 删除 tunnel。

#### 5.1.3 负向测试

建议覆盖：

- 不传 `X-Namespace`。
- 使用不存在或非本 region 的 cluster。
- 创建第 11 个 active tunnel。
- 使用非法 port。
- 查询已删除或已过期 tunnel。
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
| Gateway policy | 只允许当前 region、当前 cluster、已配置 port |
| Metering | 明细写入，tunnel 用量累加 |
| mTLS | 无客户端证书时握手失败 |

### 5.2 单元测试

单元测试重点覆盖：

- tunnel 标识、创建、查询、更新、删除。
- namespace 隔离、cluster 校验、active 过滤和配额。
- tunnel port 端口范围、重复创建、查询、更新、删除。
- metering 校验和用量累加。
- JWT TTL、缓存命中和缓存失效。
- API rate limit 和 Controller 统一响应。

开发提交前建议执行：

```bash
mvn test
```

如果改动 OpenAPI YAML，需要确认生成接口可编译，Controller 实现一致，其他微服务依赖的 path、字段名、错误码没有非预期变化。

如果改动数据库，需要新增 Flyway migration，并确认已有环境迁移路径明确。
