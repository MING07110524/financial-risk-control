import type { AssessmentService } from "@/services/contracts";
import type { AssessmentDetailVO, AssessmentIndexResultVO, AssessmentVO, RiskDataDetailVO, RiskLevel } from "@/types/risk";
import type { MockAssessment, MockDb, MockWarning } from "@/mock/schema";
import { getMockDb, nextId, updateMockDb } from "@/mock/db";
import {
  ensureRole,
  failure,
  getNowString,
  inDateRange,
  listEnabledIndexes,
  normalizeKeyword,
  paginate,
  roundScore,
  success,
  withDelay,
} from "@/services/mock/helpers";

function resolveRiskLevel(totalScore: number): RiskLevel {
  if (totalScore >= 80) {
    return "HIGH";
  }
  if (totalScore >= 60) {
    return "MEDIUM";
  }
  return "LOW";
}

function createWarningCode(warningId: number): string {
  const date = getNowString().slice(0, 10).split("-").join("");
  return `WARN-${date}-${`${warningId}`.padStart(3, "0")}`;
}

function buildWarningContent(riskLevel: RiskLevel, customerName: string, businessNo: string): string {
  const riskText = riskLevel === "HIGH" ? "高风险" : "中风险";
  return `${customerName} 的业务 ${businessNo} 评估结果为${riskText}，请尽快跟进。`;
}

function buildAssessmentIndexResults(db: MockDb, riskData: RiskDataDetailVO) {
  const enabledIndexes = listEnabledIndexes(db);
  const results: AssessmentIndexResultVO[] = [];

  for (const index of enabledIndexes) {
    const indexValue = riskData.indexValues.find((item) => item.indexId === index.id);
    if (!indexValue) {
      return failure(42004, "该业务数据缺少启用指标值，无法执行评估", [] as AssessmentIndexResultVO[]);
    }

    const rule = db.riskRules.find(
      (item) =>
        item.indexId === index.id &&
        indexValue.indexValue >= item.scoreMin &&
        indexValue.indexValue <= item.scoreMax,
    );

    if (!rule) {
      return failure(42005, `指标 ${index.indexName} 缺少可用评分规则`, [] as AssessmentIndexResultVO[]);
    }

    results.push({
      indexId: index.id,
      indexCode: index.indexCode,
      indexName: index.indexName,
      indexValue: indexValue.indexValue,
      weightValue: index.weightValue,
      scoreValue: rule.scoreValue,
      weightedScore: roundScore((rule.scoreValue * index.weightValue) / 100),
      warningLevel: rule.warningLevel,
    });
  }

  return success(results);
}

function syncAssessmentWarningInfo(db: MockDb, assessment: MockAssessment): AssessmentDetailVO {
  const warning = db.warnings.find((item) => item.assessmentId === assessment.id);
  if (!warning) {
    return {
      ...assessment,
      warningGenerated: false,
      warningInfo: null,
    };
  }

  return {
    ...assessment,
    warningGenerated: true,
    warningInfo: {
      warningId: warning.id,
      warningCode: warning.warningCode,
      warningLevel: warning.warningLevel,
      warningStatus: warning.warningStatus,
      warningContent: warning.warningContent,
    },
  };
}

function toAssessmentVO(db: MockDb, assessment: MockAssessment): AssessmentVO {
  return {
    id: assessment.id,
    riskDataId: assessment.riskDataId,
    businessNo: assessment.businessNo,
    customerName: assessment.customerName,
    totalScore: assessment.totalScore,
    riskLevel: assessment.riskLevel,
    assessmentStatus: assessment.assessmentStatus,
    assessmentTime: assessment.assessmentTime,
    assessmentByName: assessment.assessmentByName,
    warningGenerated: db.warnings.some((item) => item.assessmentId === assessment.id),
  };
}

export const mockAssessmentService: AssessmentService = {
  async pageAssessments(query) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const businessKeyword = normalizeKeyword(query.businessNo);
    const db = getMockDb();
    const records = db.assessments
      .filter((item) => !businessKeyword || item.businessNo.toLowerCase().includes(businessKeyword))
      .filter((item) => !query.riskLevel || item.riskLevel === query.riskLevel)
      .filter((item) => query.assessmentStatus === "" || item.assessmentStatus === query.assessmentStatus)
      .filter((item) => !query.riskDataId || item.riskDataId === query.riskDataId)
      .filter((item) => inDateRange(item.assessmentTime, query.startTime, query.endTime))
      .sort((left, right) => right.assessmentTime.localeCompare(left.assessmentTime))
      .map((item) => toAssessmentVO(db, item));

    return withDelay(success(paginate(records, query.pageNum, query.pageSize)));
  },
  async getAssessmentDetail(id) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const db = getMockDb();
    const assessment = db.assessments.find((item) => item.id === id);
    if (!assessment) {
      return withDelay(failure(40400, "未找到对应的评估记录", undefined as never));
    }

    return withDelay(success(syncAssessmentWarningInfo(db, assessment)));
  },
  async executeAssessment(riskDataId) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const db = getMockDb();
    const riskData = db.riskData.find((item) => item.id === riskDataId);
    if (!riskData) {
      return withDelay(failure(40400, "未找到对应的风险数据", undefined as never));
    }

    const indexResult = buildAssessmentIndexResults(db, riskData);
    if (indexResult.code !== 0) {
      return withDelay(failure(indexResult.code, indexResult.message, undefined as never));
    }

    const now = getNowString();
    const nextDb = updateMockDb((draft) => {
      draft.assessments = draft.assessments.map((item) =>
        item.riskDataId === riskDataId && item.assessmentStatus === 1
          ? { ...item, assessmentStatus: 0 }
          : item,
      );

      const currentRiskData = draft.riskData.find((item) => item.id === riskDataId);
      if (!currentRiskData) {
        return draft;
      }

      const assessmentId = nextId(draft, "assessment");
      const totalScore = roundScore(indexResult.data.reduce((sum, item) => sum + item.weightedScore, 0));
      const riskLevel = resolveRiskLevel(totalScore);
      const newAssessment: MockAssessment = {
        id: assessmentId,
        riskDataId,
        businessNo: currentRiskData.businessNo,
        customerName: currentRiskData.customerName,
        totalScore,
        riskLevel,
        assessmentStatus: 1,
        assessmentTime: now,
        assessmentBy: currentUser.userId,
        assessmentByName: currentUser.realName,
        warningGenerated: riskLevel !== "LOW",
        businessType: currentRiskData.businessType,
        riskDesc: currentRiskData.riskDesc,
        dataStatus: 1,
        indexResults: indexResult.data,
        warningInfo: null,
      };

      currentRiskData.dataStatus = 1;

      if (riskLevel !== "LOW") {
        const warningId = nextId(draft, "warning");
        const warning: MockWarning = {
          id: warningId,
          assessmentId,
          riskDataId,
          warningCode: createWarningCode(warningId),
          warningLevel: riskLevel,
          warningContent: buildWarningContent(riskLevel, currentRiskData.customerName, currentRiskData.businessNo),
          businessNo: currentRiskData.businessNo,
          customerName: currentRiskData.customerName,
          warningStatus: 0,
          createTime: now,
          businessType: currentRiskData.businessType,
          riskDesc: currentRiskData.riskDesc,
          totalScore,
          riskLevel,
          handleRecords: [],
        };
        newAssessment.warningInfo = {
          warningId: warning.id,
          warningCode: warning.warningCode,
          warningLevel: warning.warningLevel,
          warningStatus: warning.warningStatus,
          warningContent: warning.warningContent,
        };
        draft.warnings.unshift(warning);
      }

      draft.assessments.unshift(newAssessment);
      return draft;
    });

    const created = nextDb.assessments.find((item) => item.riskDataId === riskDataId && item.assessmentStatus === 1);
    if (!created) {
      return withDelay(failure(50000, "执行评估失败，请稍后再试", undefined as never));
    }

    return withDelay(success(syncAssessmentWarningInfo(nextDb, created), "评估完成"));
  },
};
