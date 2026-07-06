CREATE TABLE IF NOT EXISTS grid (
    _id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    grid VARCHAR(128) NOT NULL COMMENT 'grid name',
    region VARCHAR(128) NOT NULL COMMENT 'region',
    created_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'created unix seconds',
    updated_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'updated unix seconds',
    PRIMARY KEY (_id),
    UNIQUE KEY uk_grid (grid),
    KEY idx_region (region)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Grid information';

CREATE TABLE IF NOT EXISTS tunnel (
    _id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    name VARCHAR(128) NOT NULL COMMENT 'tunnel name',
    tunnel_id VARCHAR(32) NOT NULL COMMENT 'hex tunnel code',
    tunnel_code BIGINT UNSIGNED NOT NULL COMMENT '40bit tunnel code',
    grid_name VARCHAR(128) NOT NULL COMMENT 'grid name',
    expiration INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'expiration unix seconds',
    namespace VARCHAR(128) NOT NULL COMMENT 'namespace',
    description VARCHAR(512) DEFAULT NULL COMMENT 'description',
    cluster VARCHAR(128) DEFAULT NULL COMMENT 'cluster',
    bandwidth_used BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'used bytes',
    url VARCHAR(512) NOT NULL COMMENT 'public url',
    type VARCHAR(64) NOT NULL DEFAULT 'bridge' COMMENT 'tunnel type: bridge/env',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'soft delete flag',
    created_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'created unix seconds',
    updated_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'updated unix seconds',
    PRIMARY KEY (_id),
    UNIQUE KEY uk_tunnel_id (tunnel_id),
    UNIQUE KEY uk_tunnel_code (tunnel_code),
    KEY idx_namespace (namespace),
    KEY idx_grid_name (grid_name),
    KEY idx_namespace_deleted (namespace, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tunnel metadata';

CREATE TABLE IF NOT EXISTS metering (
    _id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    grid_name VARCHAR(128) NOT NULL COMMENT 'grid name',
    tunnel_code BIGINT UNSIGNED NOT NULL COMMENT 'tunnel code',
    tunnel_id VARCHAR(32) NOT NULL COMMENT 'tunnel id',
    usage_bytes BIGINT UNSIGNED NOT NULL COMMENT 'usage bytes',
    reported_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'reported unix seconds',
    created_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'created unix seconds',
    PRIMARY KEY (_id),
    KEY idx_grid_name (grid_name),
    KEY idx_tunnel_id (tunnel_id),
    KEY idx_tunnel_code (tunnel_code),
    KEY idx_reported_at (reported_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Metering report';

CREATE TABLE IF NOT EXISTS tunnel_port (
    _id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    tunnel_code BIGINT UNSIGNED NOT NULL COMMENT 'tunnel code',
    port BIGINT UNSIGNED NOT NULL COMMENT 'port, business range 1-65535',
    allow_anonymous TINYINT NOT NULL DEFAULT 0 COMMENT 'allow anonymous access',
    PRIMARY KEY (_id),
    UNIQUE KEY uk_tunnel_code_port (tunnel_code, port),
    KEY idx_tunnel_code (tunnel_code),
    KEY idx_port (port)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tunnel port policy';

INSERT INTO grid (grid, region, created_at, updated_at)
VALUES ('grid-a', 'region-a', UNIX_TIMESTAMP(), UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE region = 'region-a', updated_at = UNIX_TIMESTAMP();
