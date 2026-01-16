-- 系统表 主要存一些系统数据以及持久化其他服务网站的cookie信息
CREATE TABLE IF NOT EXISTS dh_system
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    key         TEXT      NOT NULL UNIQUE,
    value       TEXT      NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- k8s 环境信息 主要是type字段 保存k8s环境的类型
CREATE TABLE IF NOT EXISTS dh_k8s_environment
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT      NOT NULL,
    description TEXT               DEFAULT NULL,
    type        TEXT      NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_name ON dh_k8s_environment (name);


-- k8s 服务器 存储k8s环境下的k8s服务器信息
CREATE TABLE IF NOT EXISTS dh_k8s_server
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    environment_id INTEGER   NOT NULL,
    ip             TEXT      NOT NULL,
    port           INTEGER   NOT NULL DEFAULT 22,
    user_name      TEXT      NOT NULL DEFAULT 'root',
    password       TEXT      NOT NULL,
    create_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_environment_id ON dh_k8s_server (environment_id);

-- 服务代理功能下k8s代理记录
CREATE TABLE IF NOT EXISTS dh_k8s_proxy_record
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    env_name    TEXT      NOT NULL,
    proxy_host  TEXT      NOT NULL,
    proxy_port  INTEGER   NOT NULL,
    src_host    TEXT      NOT NULL,
    src_port    INTEGER   NOT NULL,
    description TEXT               DEFAULT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_env_name ON dh_k8s_proxy_record (env_name);
CREATE INDEX IF NOT EXISTS idx_proxy_host ON dh_k8s_proxy_record (proxy_host);
CREATE INDEX IF NOT EXISTS idx_src_host ON dh_k8s_proxy_record (src_host);
CREATE INDEX IF NOT EXISTS idx_create_time ON dh_k8s_proxy_record (create_time);

-- 操作日志
CREATE TABLE IF NOT EXISTS dh_operation_log
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user           TEXT      NOT NULL,
    request_id     TEXT      NOT NULL,
    operation      TEXT      NOT NULL,
    request_detail TEXT,
    success        BOOLEAN,
    result_detail  TEXT,
    start_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time       TIMESTAMP          DEFAULT CURRENT_TIMESTAMP
);
-- 为user字段创建普通索引
CREATE INDEX IF NOT EXISTS idx_user ON dh_operation_log (user);
-- 为request_id字段创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_request_id ON dh_operation_log (request_id);
-- 为success字段创建普通索引
CREATE INDEX IF NOT EXISTS idx_success ON dh_operation_log (success);
-- 为start_time字段创建普通索引
CREATE INDEX IF NOT EXISTS idx_start_time ON dh_operation_log (start_time);
-- 为end_time字段创建普通索引
CREATE INDEX IF NOT EXISTS idx_end_time ON dh_operation_log (end_time);
