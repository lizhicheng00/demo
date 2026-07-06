ALTER TABLE tunnel
    MODIFY tunnel_id VARCHAR(32) NOT NULL COMMENT 'base32 encoded 40bit tunnel code';

ALTER TABLE metering
    MODIFY tunnel_id VARCHAR(32) NOT NULL COMMENT 'base32 tunnel id';
