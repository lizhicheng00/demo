# Go Demo 项目

一个基于 Go 标准库的 HTTP demo 服务，包含主页、健康检查接口和时间接口。

## 本地启动

```bash
go run ./cmd/demo
```

默认监听：

```text
http://127.0.0.1:8080/
```

也可以指定端口：

```bash
PORT=5173 go run ./cmd/demo
```

## 接口

```text
GET /          主页
GET /healthz   健康检查
GET /api/time  当前时间
```

## 测试

```bash
go test ./...
```
