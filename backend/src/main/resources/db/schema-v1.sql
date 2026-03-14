CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User primary key',
    username VARCHAR(50) NOT NULL COMMENT 'Login username',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt password hash',
    real_name VARCHAR(50) NOT NULL COMMENT 'Real name',
    phone VARCHAR(20) DEFAULT NULL COMMENT 'Phone number',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System user';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Role primary key',
    role_name VARCHAR(50) NOT NULL COMMENT 'Role name',
    role_code VARCHAR(50) NOT NULL COMMENT 'Role code',
    remark VARCHAR(200) DEFAULT NULL COMMENT 'Role remark',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System role';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id),
    UNIQUE KEY uk_sys_user_role_user_id (user_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User role mapping';

CREATE TABLE IF NOT EXISTS risk_data (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    business_no VARCHAR(64) NOT NULL COMMENT 'Business number',
    customer_name VARCHAR(100) NOT NULL COMMENT 'Customer name',
    business_type VARCHAR(50) DEFAULT NULL COMMENT 'Business type',
    risk_desc VARCHAR(255) DEFAULT NULL COMMENT 'Risk description',
    data_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 assessed, 2 pending reassessment',
    create_by BIGINT NOT NULL COMMENT 'Created by user ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_data_business_no (business_no),
    KEY idx_risk_data_create_by (create_by),
    CONSTRAINT fk_risk_data_create_by FOREIGN KEY (create_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk data';

CREATE TABLE IF NOT EXISTS risk_index (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    index_name VARCHAR(100) NOT NULL COMMENT 'Index name',
    index_code VARCHAR(50) NOT NULL COMMENT 'Index code',
    weight_value DECIMAL(5,2) NOT NULL COMMENT 'Weight percentage',
    index_desc VARCHAR(255) DEFAULT NULL COMMENT 'Index description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_index_index_code (index_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk index';

CREATE TABLE IF NOT EXISTS risk_rule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    index_id BIGINT NOT NULL COMMENT 'Risk index ID',
    score_min DECIMAL(10,2) NOT NULL COMMENT 'Minimum value',
    score_max DECIMAL(10,2) NOT NULL COMMENT 'Maximum value',
    score_value DECIMAL(10,2) NOT NULL COMMENT 'Score value',
    warning_level VARCHAR(20) DEFAULT NULL COMMENT 'Suggested warning level',
    PRIMARY KEY (id),
    KEY idx_risk_rule_index_id (index_id),
    CONSTRAINT fk_risk_rule_index_id FOREIGN KEY (index_id) REFERENCES risk_index (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk rule';

CREATE TABLE IF NOT EXISTS risk_data_index_value (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    risk_data_id BIGINT NOT NULL COMMENT 'Risk data ID',
    index_id BIGINT NOT NULL COMMENT 'Risk index ID',
    index_value DECIMAL(10,2) NOT NULL COMMENT 'Index numeric value',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_data_index_value (risk_data_id, index_id),
    KEY idx_risk_data_index_value_index_id (index_id),
    CONSTRAINT fk_risk_data_index_value_risk_data_id FOREIGN KEY (risk_data_id) REFERENCES risk_data (id),
    CONSTRAINT fk_risk_data_index_value_index_id FOREIGN KEY (index_id) REFERENCES risk_index (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk data index value';

CREATE TABLE IF NOT EXISTS risk_assessment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    risk_data_id BIGINT NOT NULL COMMENT 'Risk data ID',
    total_score DECIMAL(10,2) NOT NULL COMMENT 'Total score',
    risk_level VARCHAR(20) NOT NULL COMMENT 'Risk level',
    assessment_status TINYINT NOT NULL DEFAULT 1 COMMENT '1 current, 0 invalid',
    assessment_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Assessment time',
    assessment_by BIGINT NOT NULL COMMENT 'Assessed by user ID',
    PRIMARY KEY (id),
    KEY idx_risk_assessment_risk_data_id (risk_data_id),
    KEY idx_risk_assessment_assessment_by (assessment_by),
    CONSTRAINT fk_risk_assessment_risk_data_id FOREIGN KEY (risk_data_id) REFERENCES risk_data (id),
    CONSTRAINT fk_risk_assessment_assessment_by FOREIGN KEY (assessment_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk assessment';

CREATE TABLE IF NOT EXISTS risk_assessment_index_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    assessment_id BIGINT NOT NULL COMMENT 'Assessment ID',
    index_id BIGINT NOT NULL COMMENT 'Index ID',
    index_code VARCHAR(50) NOT NULL COMMENT 'Index code snapshot',
    index_name VARCHAR(100) NOT NULL COMMENT 'Index name snapshot',
    index_value DECIMAL(10,2) NOT NULL COMMENT 'Index value snapshot',
    weight_value DECIMAL(5,2) NOT NULL COMMENT 'Weight snapshot',
    score_value DECIMAL(10,2) NOT NULL COMMENT 'Score snapshot',
    weighted_score DECIMAL(10,2) NOT NULL COMMENT 'Weighted score snapshot',
    warning_level VARCHAR(20) DEFAULT NULL COMMENT 'Warning level snapshot',
    PRIMARY KEY (id),
    KEY idx_risk_assessment_index_result_assessment_id (assessment_id),
    KEY idx_risk_assessment_index_result_index_id (index_id),
    CONSTRAINT fk_risk_assessment_index_result_assessment_id FOREIGN KEY (assessment_id) REFERENCES risk_assessment (id),
    CONSTRAINT fk_risk_assessment_index_result_index_id FOREIGN KEY (index_id) REFERENCES risk_index (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk assessment index result';

CREATE TABLE IF NOT EXISTS risk_warning (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    assessment_id BIGINT NOT NULL COMMENT 'Assessment ID',
    warning_code VARCHAR(64) NOT NULL COMMENT 'Warning code',
    warning_level VARCHAR(20) NOT NULL COMMENT 'Warning level',
    warning_content VARCHAR(255) NOT NULL COMMENT 'Warning content',
    warning_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 processing, 2 finished',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_warning_warning_code (warning_code),
    UNIQUE KEY uk_risk_warning_assessment_id (assessment_id),
    CONSTRAINT fk_risk_warning_assessment_id FOREIGN KEY (assessment_id) REFERENCES risk_assessment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Risk warning';

CREATE TABLE IF NOT EXISTS warning_handle_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    warning_id BIGINT NOT NULL COMMENT 'Warning ID',
    handle_user_id BIGINT NOT NULL COMMENT 'Handled by user ID',
    handle_opinion VARCHAR(500) NOT NULL COMMENT 'Handle opinion',
    handle_result VARCHAR(255) NOT NULL COMMENT 'Handle result',
    next_status TINYINT NOT NULL COMMENT 'Next status after handling',
    handle_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Handle time',
    PRIMARY KEY (id),
    KEY idx_warning_handle_record_warning_id (warning_id),
    KEY idx_warning_handle_record_handle_user_id (handle_user_id),
    CONSTRAINT fk_warning_handle_record_warning_id FOREIGN KEY (warning_id) REFERENCES risk_warning (id),
    CONSTRAINT fk_warning_handle_record_handle_user_id FOREIGN KEY (handle_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Warning handle record';

SET @warning_handle_record_has_next_status := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'warning_handle_record'
      AND COLUMN_NAME = 'next_status'
);

SET @warning_handle_record_next_status_ddl := IF(
    @warning_handle_record_has_next_status = 0,
    'ALTER TABLE warning_handle_record ADD COLUMN next_status TINYINT NOT NULL DEFAULT 2 COMMENT ''Next status after handling'' AFTER handle_result',
    'SELECT 1'
);

PREPARE warning_handle_record_next_status_stmt FROM @warning_handle_record_next_status_ddl;
EXECUTE warning_handle_record_next_status_stmt;
DEALLOCATE PREPARE warning_handle_record_next_status_stmt;

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT DEFAULT NULL COMMENT 'Operator user ID',
    operator VARCHAR(50) DEFAULT NULL COMMENT 'Operator username',
    module_name VARCHAR(50) NOT NULL COMMENT 'Module name',
    operation_type VARCHAR(50) NOT NULL COMMENT 'Operation type',
    operation_desc VARCHAR(255) DEFAULT NULL COMMENT 'Operation description',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Operation time',
    PRIMARY KEY (id),
    KEY idx_sys_log_user_id (user_id),
    KEY idx_sys_log_module_name (module_name),
    KEY idx_sys_log_operator (operator),
    KEY idx_sys_log_operation_time (operation_time),
    CONSTRAINT fk_sys_log_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System log';
