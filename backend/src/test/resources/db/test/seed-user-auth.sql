DELETE FROM sys_log;
DELETE FROM warning_handle_record;
DELETE FROM risk_warning;
DELETE FROM risk_assessment_index_result;
DELETE FROM risk_assessment;
DELETE FROM sys_user_role;
DELETE FROM sys_user;
DELETE FROM sys_role;
DELETE FROM risk_data_index_value;
DELETE FROM risk_rule;
DELETE FROM risk_index;
DELETE FROM risk_data;

INSERT INTO sys_role (id, role_name, role_code, remark) VALUES
    (1, '系统管理员', 'ADMIN', '系统管理、权限控制与后台维护'),
    (2, '风控人员', 'RISK_USER', '风险数据录入、评估与预警处理'),
    (3, '管理人员', 'MANAGER', '预警与统计只读查看');

INSERT INTO sys_user (id, username, password, real_name, phone, status) VALUES
    (1, 'admin-demo', '$2a$10$Ci8xD9yuuu/cA40SdTWWNuaUO0W/P2rvgDE67R8yLvH7GxJcOBNl2', '演示管理员', '13800000001', 1),
    (2, 'risk-demo', '$2a$10$Ci8xD9yuuu/cA40SdTWWNuaUO0W/P2rvgDE67R8yLvH7GxJcOBNl2', '演示风控员', '13800000002', 1),
    (3, 'manager-demo', '$2a$10$Ci8xD9yuuu/cA40SdTWWNuaUO0W/P2rvgDE67R8yLvH7GxJcOBNl2', '演示管理者', '13800000003', 1);

INSERT INTO sys_user_role (id, user_id, role_id) VALUES
    (1, 1, 1),
    (2, 2, 2),
    (3, 3, 3);

INSERT INTO risk_index (id, index_name, index_code, weight_value, index_desc, status) VALUES
    (1, '负债率', 'DEBT_RATIO', 30.00, '衡量企业负债压力，越高风险越大。', 1),
    (2, '现金流覆盖率', 'CASH_FLOW_COVERAGE', 25.00, '衡量经营现金流覆盖债务能力，越高越稳健。', 1),
    (3, '逾期次数', 'OVERDUE_COUNT', 25.00, '衡量历史逾期表现，次数越多风险越高。', 1),
    (4, '抵押覆盖率', 'COLLATERAL_COVERAGE', 20.00, '衡量抵押物覆盖程度，越高越安全。', 1);

INSERT INTO risk_rule (id, index_id, score_min, score_max, score_value, warning_level) VALUES
    (1, 1, 0.00, 40.00, 20.00, 'LOW'),
    (2, 1, 40.01, 70.00, 60.00, 'MEDIUM'),
    (3, 1, 70.01, 999.00, 90.00, 'HIGH'),
    (4, 2, 0.00, 0.99, 90.00, 'HIGH'),
    (5, 2, 1.00, 1.59, 60.00, 'MEDIUM'),
    (6, 2, 1.60, 999.00, 20.00, 'LOW'),
    (7, 3, 0.00, 0.00, 20.00, 'LOW'),
    (8, 3, 1.00, 2.00, 60.00, 'MEDIUM'),
    (9, 3, 3.00, 999.00, 90.00, 'HIGH'),
    (10, 4, 0.00, 99.99, 90.00, 'HIGH'),
    (11, 4, 100.00, 149.99, 60.00, 'MEDIUM'),
    (12, 4, 150.00, 999.00, 20.00, 'LOW');

INSERT INTO risk_data (id, business_no, customer_name, business_type, risk_desc, data_status, create_by, create_time, update_time) VALUES
    (1, 'FRC-202603-001', '星河贸易有限公司', '企业贷款', '新客户首次授信，待完成人工评估。', 0, 2, '2026-03-10 09:10:00', '2026-03-10 09:10:00'),
    (2, 'FRC-202603-002', '晨光制造股份有限公司', '流动资金贷款', '经营稳定，已有一次低风险评估记录。', 1, 2, '2026-03-09 11:20:00', '2026-03-09 11:20:00'),
    (3, 'FRC-202603-003', '远航物流集团', '供应链融资', '业务规模较大，当前存在中风险预警待处理。', 1, 2, '2026-03-08 15:30:00', '2026-03-08 15:30:00'),
    (4, 'FRC-202603-004', '宏达置业有限公司', '项目融资', '历史上出现过高风险预警，已完成处理。', 1, 2, '2026-03-07 16:15:00', '2026-03-07 16:15:00'),
    (5, 'FRC-202603-005', '云峰科技有限公司', '保函业务', '业务数据已更新，等待重新评估。', 2, 2, '2026-03-06 10:05:00', '2026-03-11 14:40:00');

INSERT INTO risk_data_index_value (risk_data_id, index_id, index_value) VALUES
    (1, 1, 68.00), (1, 2, 1.25), (1, 3, 1.00), (1, 4, 130.00),
    (2, 1, 35.00), (2, 2, 1.80), (2, 3, 0.00), (2, 4, 170.00),
    (3, 1, 65.00), (3, 2, 1.20), (3, 3, 2.00), (3, 4, 120.00),
    (4, 1, 85.00), (4, 2, 0.70), (4, 3, 4.00), (4, 4, 80.00),
    (5, 1, 75.00), (5, 2, 0.90), (5, 3, 3.00), (5, 4, 95.00);

INSERT INTO risk_assessment (id, risk_data_id, total_score, risk_level, assessment_status, assessment_time, assessment_by) VALUES
    (1, 2, 20.00, 'LOW', 1, '2026-03-09 11:35:00', 2),
    (2, 3, 60.00, 'MEDIUM', 1, '2026-03-08 15:45:00', 2),
    (3, 4, 90.00, 'HIGH', 1, '2026-03-07 16:30:00', 2),
    (4, 5, 75.00, 'MEDIUM', 0, '2026-03-10 10:10:00', 2);

INSERT INTO risk_assessment_index_result (assessment_id, index_id, index_code, index_name, index_value, weight_value, score_value, weighted_score, warning_level) VALUES
    (1, 1, 'DEBT_RATIO', '负债率', 35.00, 30.00, 20.00, 6.00, 'LOW'),
    (1, 2, 'CASH_FLOW_COVERAGE', '现金流覆盖率', 1.80, 25.00, 20.00, 5.00, 'LOW'),
    (1, 3, 'OVERDUE_COUNT', '逾期次数', 0.00, 25.00, 20.00, 5.00, 'LOW'),
    (1, 4, 'COLLATERAL_COVERAGE', '抵押覆盖率', 170.00, 20.00, 20.00, 4.00, 'LOW'),
    (2, 1, 'DEBT_RATIO', '负债率', 65.00, 30.00, 60.00, 18.00, 'MEDIUM'),
    (2, 2, 'CASH_FLOW_COVERAGE', '现金流覆盖率', 1.20, 25.00, 60.00, 15.00, 'MEDIUM'),
    (2, 3, 'OVERDUE_COUNT', '逾期次数', 2.00, 25.00, 60.00, 15.00, 'MEDIUM'),
    (2, 4, 'COLLATERAL_COVERAGE', '抵押覆盖率', 120.00, 20.00, 60.00, 12.00, 'MEDIUM'),
    (3, 1, 'DEBT_RATIO', '负债率', 85.00, 30.00, 90.00, 27.00, 'HIGH'),
    (3, 2, 'CASH_FLOW_COVERAGE', '现金流覆盖率', 0.70, 25.00, 90.00, 22.50, 'HIGH'),
    (3, 3, 'OVERDUE_COUNT', '逾期次数', 4.00, 25.00, 90.00, 22.50, 'HIGH'),
    (3, 4, 'COLLATERAL_COVERAGE', '抵押覆盖率', 80.00, 20.00, 90.00, 18.00, 'HIGH'),
    (4, 1, 'DEBT_RATIO', '负债率', 75.00, 30.00, 90.00, 27.00, 'HIGH'),
    (4, 2, 'CASH_FLOW_COVERAGE', '现金流覆盖率', 0.90, 25.00, 60.00, 15.00, 'MEDIUM'),
    (4, 3, 'OVERDUE_COUNT', '逾期次数', 3.00, 25.00, 60.00, 15.00, 'MEDIUM'),
    (4, 4, 'COLLATERAL_COVERAGE', '抵押覆盖率', 95.00, 20.00, 90.00, 18.00, 'HIGH');

INSERT INTO risk_warning (id, assessment_id, warning_code, warning_level, warning_content, warning_status, create_time) VALUES
    (1, 2, 'WARN-20260308-001', 'MEDIUM', '远航物流集团 的业务 FRC-202603-003 评估结果为中风险，请尽快跟进。', 0, '2026-03-08 15:46:00'),
    (2, 3, 'WARN-20260307-001', 'HIGH', '宏达置业有限公司 的业务 FRC-202603-004 评估结果为高风险，请尽快跟进。', 2, '2026-03-07 16:31:00');

INSERT INTO warning_handle_record (id, warning_id, handle_user_id, handle_opinion, handle_result, next_status, handle_time) VALUES
    (1, 2, 2, '已联系客户补充抵押物并收紧授信额度。', '完成首次处置，风险已纳入重点跟踪。', 2, '2026-03-08 09:30:00');
