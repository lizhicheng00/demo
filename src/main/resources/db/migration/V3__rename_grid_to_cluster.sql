RENAME TABLE grid TO cluster;

ALTER TABLE cluster
    CHANGE COLUMN grid cluster_id VARCHAR(128) NOT NULL COMMENT 'cluster identifier',
    RENAME INDEX uk_grid TO uk_cluster_id;

ALTER TABLE tunnel
    CHANGE COLUMN grid_name cluster_id VARCHAR(128) NOT NULL COMMENT 'cluster identifier',
    DROP COLUMN cluster,
    RENAME INDEX idx_grid_name TO idx_cluster_id;

ALTER TABLE metering
    CHANGE COLUMN grid_name cluster_id VARCHAR(128) NOT NULL COMMENT 'cluster identifier',
    RENAME INDEX idx_grid_name TO idx_cluster_id;

UPDATE tunnel SET cluster_id = 'cluster-a' WHERE cluster_id = 'grid-a';
UPDATE metering SET cluster_id = 'cluster-a' WHERE cluster_id = 'grid-a';
UPDATE cluster SET cluster_id = 'cluster-a' WHERE cluster_id = 'grid-a';
