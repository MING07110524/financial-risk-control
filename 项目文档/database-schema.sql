CREATE DATABASE IF NOT EXISTS financial_risk_control
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE financial_risk_control;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态 1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色主键',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    remark VARCHAR(200) DEFAULT NULL COMMENT '角色说明',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS risk_data (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    business_no VARCHAR(64) NOT NULL COMMENT '业务编号',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    business_type VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    risk_desc VARCHAR(255) DEFAULT NULL COMMENT '风险说明',
    data_status TINYINT NOT NULL DEFAULT 0 COMMENT '数据状态 0待评估 1已评估 2待重评',
    create_by BIGINT NOT NULL COMMENT '录入人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_data_business_no (business_no),
    KEY idx_risk_data_create_by (create_by),
    CONSTRAINT fk_risk_data_create_by FOREIGN KEY (create_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险数据表';

CREATE TABLE IF NOT EXISTS risk_index (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    index_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    index_code VARCHAR(50) NOT NULL COMMENT '指标编码',
    weight_value DECIMAL(5,2) NOT NULL COMMENT '指标权重',
    index_desc VARCHAR(255) DEFAULT NULL COMMENT '指标说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_index_index_code (index_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险指标表';

CREATE TABLE IF NOT EXISTS risk_rule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    index_id BIGINT NOT NULL COMMENT '指标ID',
    score_min DECIMAL(10,2) NOT NULL COMMENT '最小值',
    score_max DECIMAL(10,2) NOT NULL COMMENT '最大值',
    score_value DECIMAL(10,2) NOT NULL COMMENT '对应分值',
    warning_level VARCHAR(20) DEFAULT NULL COMMENT '预警等级建议',
    PRIMARY KEY (id),
    KEY idx_risk_rule_index_id (index_id),
    CONSTRAINT fk_risk_rule_index_id FOREIGN KEY (index_id) REFERENCES risk_index (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分规则表';

CREATE TABLE IF NOT EXISTS risk_assessment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    risk_data_id BIGINT NOT NULL COMMENT '风险数据ID',
    total_score DECIMAL(10,2) NOT NULL COMMENT '总评分',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级',
    assessment_status TINYINT NOT NULL DEFAULT 1 COMMENT '评估状态',
    assessment_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评估时间',
    assessment_by BIGINT NOT NULL COMMENT '评估人',
    PRIMARY KEY (id),
    KEY idx_risk_assessment_risk_data_id (risk_data_id),
    KEY idx_risk_assessment_assessment_by (assessment_by),
    CONSTRAINT fk_risk_assessment_risk_data_id FOREIGN KEY (risk_data_id) REFERENCES risk_data (id),
    CONSTRAINT fk_risk_assessment_assessment_by FOREIGN KEY (assessment_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险评估结果表';

CREATE TABLE IF NOT EXISTS risk_warning (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    assessment_id BIGINT NOT NULL COMMENT '评估结果ID',
    warning_code VARCHAR(64) NOT NULL COMMENT '预警编号',
    warning_level VARCHAR(20) NOT NULL COMMENT '预警级别',
    warning_content VARCHAR(255) NOT NULL COMMENT '预警内容',
    warning_status TINYINT NOT NULL DEFAULT 0 COMMENT '预警状态 0待处理 1处理中 2已处理',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_risk_warning_warning_code (warning_code),
    KEY idx_risk_warning_assessment_id (assessment_id),
    CONSTRAINT fk_risk_warning_assessment_id FOREIGN KEY (assessment_id) REFERENCES risk_assessment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警信息表';

CREATE TABLE IF NOT EXISTS warning_handle_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    warning_id BIGINT NOT NULL COMMENT '预警ID',
    handle_user_id BIGINT NOT NULL COMMENT '处理人',
    handle_opinion VARCHAR(500) NOT NULL COMMENT '处理意见',
    handle_result VARCHAR(255) NOT NULL COMMENT '处理结果',
    handle_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    PRIMARY KEY (id),
    KEY idx_warning_handle_record_warning_id (warning_id),
    KEY idx_warning_handle_record_handle_user_id (handle_user_id),
    CONSTRAINT fk_warning_handle_record_warning_id FOREIGN KEY (warning_id) REFERENCES risk_warning (id),
    CONSTRAINT fk_warning_handle_record_handle_user_id FOREIGN KEY (handle_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警处置记录表';

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '操作人ID',
    module_name VARCHAR(50) NOT NULL COMMENT '模块名称',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc VARCHAR(255) DEFAULT NULL COMMENT '操作说明',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_sys_log_user_id (user_id),
    KEY idx_sys_log_module_name (module_name),
    CONSTRAINT fk_sys_log_user_id FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

INSERT INTO sys_role (role_name, role_code, remark)
VALUES
('系统管理员', 'ADMIN', '负责系统管理和权限分配'),
('风控人员', 'RISK_USER', '负责风险数据录入、评估和预警处理'),
('管理人员', 'MANAGER', '负责查看统计分析和预警结果')
ON DUPLICATE KEY UPDATE remark = VALUES(remark);

INSERT INTO sys_user (username, password, real_name, phone, status)
VALUES ('admin', '123456', '系统管理员', '13800000000', 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
