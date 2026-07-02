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
    tunnelid VARCHAR(32) NOT NULL COMMENT 'hex tunnel code',
    tunnelcode BIGINT UNSIGNED NOT NULL COMMENT '40bit tunnel code',
    gridname VARCHAR(128) NOT NULL COMMENT 'grid name',
    expiration INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'expiration unix seconds',
    namespace VARCHAR(128) NOT NULL COMMENT 'namespace',
    description VARCHAR(512) DEFAULT NULL COMMENT 'description',
    cluster VARCHAR(128) DEFAULT NULL COMMENT 'cluster',
    bandwidthused BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'used bytes',
    url VARCHAR(512) NOT NULL COMMENT 'public url',
    type VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT 'tunnel type',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'soft delete flag',
    created_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'created unix seconds',
    updated_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'updated unix seconds',
    PRIMARY KEY (_id),
    UNIQUE KEY uk_tunnelid (tunnelid),
    UNIQUE KEY uk_tunnelcode (tunnelcode),
    KEY idx_namespace (namespace),
    KEY idx_gridname (gridname),
    KEY idx_namespace_deleted (namespace, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tunnel metadata';

CREATE TABLE IF NOT EXISTS node_registry (
    _id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'node primary key',
    gridname VARCHAR(128) NOT NULL COMMENT 'grid name',
    ip VARCHAR(128) NOT NULL COMMENT 'gateway node ip',
    registertime BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'last register unix seconds',
    created_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'created unix seconds',
    updated_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'updated unix seconds',
    PRIMARY KEY (_id),
    KEY idx_gridname (gridname),
    KEY idx_grid_ip (gridname, ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Gateway node registry';

CREATE TABLE IF NOT EXISTS metering (
    _id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    gridname VARCHAR(128) NOT NULL COMMENT 'grid name',
    tunnelcode BIGINT UNSIGNED NOT NULL COMMENT 'tunnel code',
    tunnelid VARCHAR(32) NOT NULL COMMENT 'tunnel id',
    usage_bytes BIGINT UNSIGNED NOT NULL COMMENT 'usage bytes',
    reported_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'reported unix seconds',
    created_at BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'created unix seconds',
    PRIMARY KEY (_id),
    KEY idx_gridname (gridname),
    KEY idx_tunnelid (tunnelid),
    KEY idx_tunnelcode (tunnelcode),
    KEY idx_reported_at (reported_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Metering report';

INSERT INTO grid (grid, region, created_at, updated_at)
VALUES ('grid-a', 'region-a', UNIX_TIMESTAMP(), UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE region = VALUES(region), updated_at = UNIX_TIMESTAMP();
