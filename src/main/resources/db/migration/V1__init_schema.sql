create table app_users (
    id bigint primary key auto_increment,
    username varchar(64) not null,
    password_hash varchar(128) not null,
    roles varchar(256) not null,
    enabled bit not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uk_app_users_username unique (username)
) engine = InnoDB default charset = utf8mb4;

create table relay_domains (
    id bigint primary key auto_increment,
    domain varchar(255) not null,
    target_url varchar(512) not null,
    status varchar(32) not null,
    remark varchar(512),
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uk_relay_domains_domain unique (domain)
) engine = InnoDB default charset = utf8mb4;

create table nodes (
    id bigint primary key auto_increment,
    node_code varchar(64) not null,
    name varchar(128) not null,
    address varchar(255),
    status varchar(32) not null,
    last_heartbeat_at datetime(6),
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint uk_nodes_node_code unique (node_code)
) engine = InnoDB default charset = utf8mb4;

create table listening_configs (
    id bigint primary key auto_increment,
    relay_domain_id bigint not null,
    node_id bigint,
    listen_port int not null,
    protocol varchar(32) not null,
    status varchar(32) not null,
    version bigint not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    constraint fk_listening_configs_relay_domain foreign key (relay_domain_id) references relay_domains (id),
    constraint fk_listening_configs_node foreign key (node_id) references nodes (id),
    index idx_listening_configs_sync (status, version),
    index idx_listening_configs_node (node_id)
) engine = InnoDB default charset = utf8mb4;

create table traffic_snapshots (
    id bigint primary key auto_increment,
    node_id bigint,
    captured_at datetime(6) not null,
    inbound_bytes bigint not null,
    outbound_bytes bigint not null,
    active_connections int not null,
    constraint fk_traffic_snapshots_node foreign key (node_id) references nodes (id),
    index idx_traffic_snapshots_captured_at (captured_at),
    index idx_traffic_snapshots_node (node_id)
) engine = InnoDB default charset = utf8mb4;
