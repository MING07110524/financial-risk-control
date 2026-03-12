import type { CurrentUser } from "@/types/auth";
import type {
  AssessmentIndexResultVO,
  DashboardStatisticsVO,
  HandleSummaryStatisticsVO,
  RiskDataDetailVO,
  RiskDataStatus,
  RiskIndexVO,
  RiskLevel,
  RiskLevelStatisticsVO,
  RiskRuleVO,
  RoleOption,
  WarningHandleRecordVO,
  WarningStatus,
  WarningTrendStatisticsVO,
} from "@/types/risk";
import type { MockAssessment, MockDb, MockUser, MockWarning } from "@/mock/schema";

function roundScore(value: number): number {
  return Number(value.toFixed(2));
}

function buildAssessmentIndexResults(
  indexes: RiskIndexVO[],
  rules: RiskRuleVO[],
  detail: RiskDataDetailVO,
): AssessmentIndexResultVO[] {
  return indexes.map((index) => {
    const indexValue = detail.indexValues.find((item) => item.indexId === index.id);
    const rule = rules.find(
      (item) =>
        item.indexId === index.id &&
        indexValue &&
        indexValue.indexValue >= item.scoreMin &&
        indexValue.indexValue <= item.scoreMax,
    );

    if (!indexValue || !rule) {
      throw new Error(`Seed data is incomplete for risk index ${index.indexCode}`);
    }

    return {
      indexId: index.id,
      indexCode: index.indexCode,
      indexName: index.indexName,
      indexValue: indexValue.indexValue,
      weightValue: index.weightValue,
      scoreValue: rule.scoreValue,
      weightedScore: roundScore((rule.scoreValue * index.weightValue) / 100),
      warningLevel: rule.warningLevel,
    };
  });
}

function calculateRiskLevel(totalScore: number): RiskLevel {
  if (totalScore >= 80) {
    return "HIGH";
  }
  if (totalScore >= 60) {
    return "MEDIUM";
  }
  return "LOW";
}

function buildAssessment(
  id: number,
  riskData: RiskDataDetailVO,
  indexes: RiskIndexVO[],
  rules: RiskRuleVO[],
  assessmentTime: string,
  assessmentBy: CurrentUser,
  assessmentStatus: 0 | 1,
): MockAssessment {
  const indexResults = buildAssessmentIndexResults(indexes, rules, riskData);
  const totalScore = roundScore(indexResults.reduce((sum, item) => sum + item.weightedScore, 0));
  const riskLevel = calculateRiskLevel(totalScore);

  return {
    id,
    riskDataId: riskData.id,
    businessNo: riskData.businessNo,
    customerName: riskData.customerName,
    totalScore,
    riskLevel,
    assessmentStatus,
    assessmentTime,
    assessmentBy: assessmentBy.userId,
    assessmentByName: assessmentBy.realName,
    warningGenerated: riskLevel !== "LOW",
    businessType: riskData.businessType,
    riskDesc: riskData.riskDesc,
    dataStatus: riskData.dataStatus,
    indexResults,
    warningInfo: null,
  };
}

function createWarning(
  id: number,
  assessment: MockAssessment,
  warningCode: string,
  warningStatus: WarningStatus,
  createTime: string,
  handleRecords: WarningHandleRecordVO[],
): MockWarning {
  const warningText = assessment.riskLevel === "HIGH" ? "高风险" : "中风险";

  return {
    id,
    assessmentId: assessment.id,
    riskDataId: assessment.riskDataId,
    warningCode,
    warningLevel: assessment.riskLevel,
    warningContent: `${assessment.customerName} 的 ${assessment.businessNo} 评估结果为${warningText}，请及时处理。`,
    businessNo: assessment.businessNo,
    customerName: assessment.customerName,
    warningStatus,
    createTime,
    businessType: assessment.businessType,
    riskDesc: assessment.riskDesc,
    totalScore: assessment.totalScore,
    riskLevel: assessment.riskLevel,
    handleRecords,
  };
}

export const mockRoles: RoleOption[] = [
  { id: 1, roleCode: "ADMIN", roleName: "系统管理员", remark: "系统管理、指标规则查看" },
  { id: 2, roleCode: "RISK_USER", roleName: "风控人员", remark: "录入风险数据、执行评估和处理预警" },
  { id: 3, roleCode: "MANAGER", roleName: "管理人员", remark: "查看预警与统计分析" },
];

export const mockUsers: MockUser[] = [
  {
    id: 1,
    username: "admin-demo",
    realName: "演示管理员",
    roleId: 1,
    roleCode: "ADMIN",
    roleName: "系统管理员",
    phone: "13800000001",
    status: 1,
  },
  {
    id: 2,
    username: "risk-demo",
    realName: "演示风控员",
    roleId: 2,
    roleCode: "RISK_USER",
    roleName: "风控人员",
    phone: "13800000002",
    status: 1,
  },
  {
    id: 3,
    username: "manager-demo",
    realName: "演示管理者",
    roleId: 3,
    roleCode: "MANAGER",
    roleName: "管理人员",
    phone: "13800000003",
    status: 1,
  },
];

export const mockRiskIndexes: RiskIndexVO[] = [
  {
    id: 1,
    indexName: "负债率",
    indexCode: "DEBT_RATIO",
    weightValue: 30,
    indexDesc: "衡量企业负债压力，越高风险越大。",
    status: 1,
  },
  {
    id: 2,
    indexName: "现金流覆盖率",
    indexCode: "CASH_FLOW_COVERAGE",
    weightValue: 25,
    indexDesc: "衡量经营现金流覆盖债务能力，越高越稳健。",
    status: 1,
  },
  {
    id: 3,
    indexName: "逾期次数",
    indexCode: "OVERDUE_COUNT",
    weightValue: 25,
    indexDesc: "衡量历史逾期表现，次数越多风险越高。",
    status: 1,
  },
  {
    id: 4,
    indexName: "抵押覆盖率",
    indexCode: "COLLATERAL_COVERAGE",
    weightValue: 20,
    indexDesc: "衡量抵押物覆盖程度，越高越安全。",
    status: 1,
  },
];

export const mockRiskRules: RiskRuleVO[] = [
  { id: 1, indexId: 1, indexName: "负债率", scoreMin: 0, scoreMax: 40, scoreValue: 20, warningLevel: "LOW" },
  { id: 2, indexId: 1, indexName: "负债率", scoreMin: 40.01, scoreMax: 70, scoreValue: 60, warningLevel: "MEDIUM" },
  { id: 3, indexId: 1, indexName: "负债率", scoreMin: 70.01, scoreMax: 100, scoreValue: 90, warningLevel: "HIGH" },
  { id: 4, indexId: 2, indexName: "现金流覆盖率", scoreMin: 0, scoreMax: 0.99, scoreValue: 90, warningLevel: "HIGH" },
  { id: 5, indexId: 2, indexName: "现金流覆盖率", scoreMin: 1, scoreMax: 1.59, scoreValue: 60, warningLevel: "MEDIUM" },
  { id: 6, indexId: 2, indexName: "现金流覆盖率", scoreMin: 1.6, scoreMax: 999, scoreValue: 20, warningLevel: "LOW" },
  { id: 7, indexId: 3, indexName: "逾期次数", scoreMin: 0, scoreMax: 0, scoreValue: 20, warningLevel: "LOW" },
  { id: 8, indexId: 3, indexName: "逾期次数", scoreMin: 1, scoreMax: 2, scoreValue: 60, warningLevel: "MEDIUM" },
  { id: 9, indexId: 3, indexName: "逾期次数", scoreMin: 3, scoreMax: 99, scoreValue: 90, warningLevel: "HIGH" },
  { id: 10, indexId: 4, indexName: "抵押覆盖率", scoreMin: 0, scoreMax: 99.99, scoreValue: 90, warningLevel: "HIGH" },
  { id: 11, indexId: 4, indexName: "抵押覆盖率", scoreMin: 100, scoreMax: 149.99, scoreValue: 60, warningLevel: "MEDIUM" },
  { id: 12, indexId: 4, indexName: "抵押覆盖率", scoreMin: 150, scoreMax: 999, scoreValue: 20, warningLevel: "LOW" },
];

function createRiskData(
  id: number,
  businessNo: string,
  customerName: string,
  businessType: string,
  riskDesc: string,
  dataStatus: RiskDataStatus,
  createTime: string,
  updateTime: string,
  indexValues: Array<{ indexId: number; indexValue: number }>,
): RiskDataDetailVO {
  const creator = mockUsers.find((item) => item.roleCode === "RISK_USER");
  if (!creator) {
    throw new Error("Missing demo risk user");
  }

  return {
    id,
    businessNo,
    customerName,
    businessType,
    riskDesc,
    dataStatus,
    createBy: creator.id,
    createByName: creator.realName,
    createTime,
    updateTime,
    indexValues: indexValues.map((item) => {
      const index = mockRiskIndexes.find((entry) => entry.id === item.indexId);
      if (!index) {
        throw new Error(`Missing risk index ${item.indexId}`);
      }
      return {
        indexId: index.id,
        indexCode: index.indexCode,
        indexName: index.indexName,
        indexValue: item.indexValue,
        weightValue: index.weightValue,
      };
    }),
  };
}

export function createMockSeed(): MockDb {
  const riskData: RiskDataDetailVO[] = [
    createRiskData(
      1,
      "FRC-202603-001",
      "星河贸易有限公司",
      "企业贷款",
      "新客户首次授信，待完成人工评估。",
      0,
      "2026-03-10 09:10:00",
      "2026-03-10 09:10:00",
      [
        { indexId: 1, indexValue: 68 },
        { indexId: 2, indexValue: 1.25 },
        { indexId: 3, indexValue: 1 },
        { indexId: 4, indexValue: 130 },
      ],
    ),
    createRiskData(
      2,
      "FRC-202603-002",
      "晨光制造股份有限公司",
      "流动资金贷款",
      "经营稳定，已有一次低风险评估记录。",
      1,
      "2026-03-09 11:20:00",
      "2026-03-09 11:20:00",
      [
        { indexId: 1, indexValue: 35 },
        { indexId: 2, indexValue: 1.8 },
        { indexId: 3, indexValue: 0 },
        { indexId: 4, indexValue: 170 },
      ],
    ),
    createRiskData(
      3,
      "FRC-202603-003",
      "远航物流集团",
      "供应链融资",
      "业务规模较大，当前存在中风险预警待处理。",
      1,
      "2026-03-08 15:30:00",
      "2026-03-08 15:30:00",
      [
        { indexId: 1, indexValue: 65 },
        { indexId: 2, indexValue: 1.2 },
        { indexId: 3, indexValue: 2 },
        { indexId: 4, indexValue: 120 },
      ],
    ),
    createRiskData(
      4,
      "FRC-202603-004",
      "宏达置业有限公司",
      "项目融资",
      "历史上出现过高风险预警，已完成处理。",
      1,
      "2026-03-07 16:15:00",
      "2026-03-07 16:15:00",
      [
        { indexId: 1, indexValue: 85 },
        { indexId: 2, indexValue: 0.7 },
        { indexId: 3, indexValue: 4 },
        { indexId: 4, indexValue: 80 },
      ],
    ),
    createRiskData(
      5,
      "FRC-202603-005",
      "云峰科技有限公司",
      "保函业务",
      "业务数据已更新，等待重新评估。",
      2,
      "2026-03-06 10:05:00",
      "2026-03-11 14:40:00",
      [
        { indexId: 1, indexValue: 75 },
        { indexId: 2, indexValue: 0.9 },
        { indexId: 3, indexValue: 3 },
        { indexId: 4, indexValue: 95 },
      ],
    ),
  ];

  const riskOperator: CurrentUser = {
    userId: 2,
    username: "risk-demo",
    realName: "演示风控员",
    roleCode: "RISK_USER",
    roleName: "风控人员",
  };

  const lowRiskData = riskData[1];
  const mediumRiskData = riskData[2];
  const highRiskData = riskData[3];
  if (!lowRiskData || !mediumRiskData || !highRiskData) {
    throw new Error("Mock risk data seed is incomplete");
  }

  const historyRiskData = createRiskData(
    5,
    "FRC-202603-005",
    "云峰科技有限公司",
    "保函业务",
    "业务数据更新前的低风险评估记录。",
    1,
    "2026-03-06 10:05:00",
    "2026-03-10 10:00:00",
    [
      { indexId: 1, indexValue: 38 },
      { indexId: 2, indexValue: 1.65 },
      { indexId: 3, indexValue: 0 },
      { indexId: 4, indexValue: 165 },
    ],
  );

  const assessments: MockAssessment[] = [
    buildAssessment(1, lowRiskData, mockRiskIndexes, mockRiskRules, "2026-03-09 11:35:00", riskOperator, 1),
    buildAssessment(2, mediumRiskData, mockRiskIndexes, mockRiskRules, "2026-03-08 15:45:00", riskOperator, 1),
    buildAssessment(3, highRiskData, mockRiskIndexes, mockRiskRules, "2026-03-07 16:30:00", riskOperator, 1),
    buildAssessment(4, historyRiskData, mockRiskIndexes, mockRiskRules, "2026-03-10 10:10:00", riskOperator, 0),
  ];

  const handleRecords: WarningHandleRecordVO[] = [
    {
      id: 1,
      warningId: 2,
      handleUserId: 2,
      handleUserName: "演示风控员",
      handleOpinion: "已联系客户补充抵押物并收紧授信额度。",
      handleResult: "完成首次处置，风险已纳入重点跟踪。",
      nextStatus: 2,
      handleTime: "2026-03-08 09:30:00",
    },
  ];

  const warnings: MockWarning[] = [
    createWarning(1, assessments[1]!, "WARN-20260308-001", 0, "2026-03-08 15:46:00", []),
    createWarning(2, assessments[2]!, "WARN-20260307-001", 2, "2026-03-07 16:31:00", handleRecords),
  ];

  const pendingWarning = warnings[0];
  const handledWarning = warnings[1];
  const pendingAssessment = assessments[1];
  const handledAssessment = assessments[2];
  if (!pendingWarning || !handledWarning || !pendingAssessment || !handledAssessment) {
    throw new Error("Mock warning seed is incomplete");
  }

  pendingAssessment.warningInfo = {
    warningId: pendingWarning.id,
    warningCode: pendingWarning.warningCode,
    warningLevel: pendingWarning.warningLevel,
    warningStatus: pendingWarning.warningStatus,
    warningContent: pendingWarning.warningContent,
  };

  handledAssessment.warningInfo = {
    warningId: handledWarning.id,
    warningCode: handledWarning.warningCode,
    warningLevel: handledWarning.warningLevel,
    warningStatus: handledWarning.warningStatus,
    warningContent: handledWarning.warningContent,
  };

  return {
    roles: structuredClone(mockRoles),
    users: structuredClone(mockUsers),
    riskIndexes: structuredClone(mockRiskIndexes),
    riskRules: structuredClone(mockRiskRules),
    riskData: structuredClone(riskData),
    assessments: structuredClone(assessments),
    warnings: structuredClone(warnings),
    nextIds: {
      riskData: 6,
      assessment: 5,
      warning: 3,
      warningRecord: 2,
    },
  };
}

export function createDashboardStatistics(db: MockDb): DashboardStatisticsVO {
  return {
    riskDataCount: db.riskData.length,
    assessmentCount: db.assessments.length,
    warningCount: db.warnings.length,
    handledWarningCount: db.warnings.filter((item) => item.warningStatus === 2).length,
    highRiskCount: db.assessments.filter((item) => item.assessmentStatus === 1 && item.riskLevel === "HIGH").length,
  };
}

export function createRiskLevelStatistics(db: MockDb): RiskLevelStatisticsVO[] {
  const activeAssessments = db.assessments.filter((item) => item.assessmentStatus === 1);
  return [
    { riskLevel: "LOW", count: activeAssessments.filter((item) => item.riskLevel === "LOW").length },
    { riskLevel: "MEDIUM", count: activeAssessments.filter((item) => item.riskLevel === "MEDIUM").length },
    { riskLevel: "HIGH", count: activeAssessments.filter((item) => item.riskLevel === "HIGH").length },
  ];
}

export function createHandleSummaryStatistics(db: MockDb): HandleSummaryStatisticsVO[] {
  return [
    { warningStatus: 0, label: "待处理", count: db.warnings.filter((item) => item.warningStatus === 0).length },
    { warningStatus: 1, label: "处理中", count: db.warnings.filter((item) => item.warningStatus === 1).length },
    { warningStatus: 2, label: "已处理", count: db.warnings.filter((item) => item.warningStatus === 2).length },
  ];
}

export function createWarningTrendStatistics(db: MockDb): WarningTrendStatisticsVO[] {
  const grouped = new Map<string, WarningTrendStatisticsVO>();

  db.warnings.forEach((item) => {
    const date = item.createTime.slice(0, 10);
    const current = grouped.get(date) ?? { date, total: 0, pending: 0, handled: 0 };
    current.total += 1;
    if (item.warningStatus === 2) {
      current.handled += 1;
    } else {
      current.pending += 1;
    }
    grouped.set(date, current);
  });

  return [...grouped.values()].sort((left, right) => left.date.localeCompare(right.date));
}
