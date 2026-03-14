CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_sys_user_username ON sys_user (username);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    remark VARCHAR(200)
);

CREATE UNIQUE INDEX uk_sys_role_role_code ON sys_role (role_code);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE UNIQUE INDEX uk_sys_user_role_user_role ON sys_user_role (user_id, role_id);
CREATE UNIQUE INDEX uk_sys_user_role_user_id ON sys_user_role (user_id);

CREATE TABLE IF NOT EXISTS risk_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    business_no VARCHAR(64) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    business_type VARCHAR(50),
    risk_desc VARCHAR(255),
    data_status TINYINT NOT NULL DEFAULT 0,
    create_by BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_risk_data_business_no ON risk_data (business_no);

CREATE TABLE IF NOT EXISTS risk_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    index_name VARCHAR(100) NOT NULL,
    index_code VARCHAR(50) NOT NULL,
    weight_value DECIMAL(5,2) NOT NULL,
    index_desc VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX uk_risk_index_index_code ON risk_index (index_code);

CREATE TABLE IF NOT EXISTS risk_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    index_id BIGINT NOT NULL,
    score_min DECIMAL(10,2) NOT NULL,
    score_max DECIMAL(10,2) NOT NULL,
    score_value DECIMAL(10,2) NOT NULL,
    warning_level VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS risk_data_index_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    risk_data_id BIGINT NOT NULL,
    index_id BIGINT NOT NULL,
    index_value DECIMAL(10,2) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_risk_data_index_value ON risk_data_index_value (risk_data_id, index_id);

CREATE TABLE IF NOT EXISTS risk_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    risk_data_id BIGINT NOT NULL,
    total_score DECIMAL(10,2) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    assessment_status TINYINT NOT NULL DEFAULT 1,
    assessment_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assessment_by BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS risk_assessment_index_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assessment_id BIGINT NOT NULL,
    index_id BIGINT NOT NULL,
    index_code VARCHAR(50) NOT NULL,
    index_name VARCHAR(100) NOT NULL,
    index_value DECIMAL(10,2) NOT NULL,
    weight_value DECIMAL(5,2) NOT NULL,
    score_value DECIMAL(10,2) NOT NULL,
    weighted_score DECIMAL(10,2) NOT NULL,
    warning_level VARCHAR(20)
);

CREATE INDEX idx_risk_assessment_index_result_assessment_id ON risk_assessment_index_result (assessment_id);

CREATE TABLE IF NOT EXISTS risk_warning (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assessment_id BIGINT NOT NULL,
    warning_code VARCHAR(64) NOT NULL,
    warning_level VARCHAR(20) NOT NULL,
    warning_content VARCHAR(255) NOT NULL,
    warning_status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_risk_warning_warning_code ON risk_warning (warning_code);
CREATE UNIQUE INDEX uk_risk_warning_assessment_id ON risk_warning (assessment_id);

CREATE TABLE IF NOT EXISTS warning_handle_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warning_id BIGINT NOT NULL,
    handle_user_id BIGINT NOT NULL,
    handle_opinion VARCHAR(500) NOT NULL,
    handle_result VARCHAR(255) NOT NULL,
    next_status TINYINT NOT NULL,
    handle_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_warning_handle_record_warning_id ON warning_handle_record (warning_id);

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    operator VARCHAR(50),
    module_name VARCHAR(50) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operation_desc VARCHAR(255),
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sys_log_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id)
);

CREATE INDEX idx_sys_log_user_id ON sys_log (user_id);
CREATE INDEX idx_sys_log_module_name ON sys_log (module_name);
CREATE INDEX idx_sys_log_operator ON sys_log (operator);
CREATE INDEX idx_sys_log_operation_time ON sys_log (operation_time);
