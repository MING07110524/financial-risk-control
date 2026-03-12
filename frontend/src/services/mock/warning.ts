import type { WarningService } from "@/services/contracts";
import type { WarningDetailVO, WarningHandleRecordVO, WarningVO } from "@/types/risk";
import { getMockDb, nextId, updateMockDb } from "@/mock/db";
import {
  ensureRole,
  failure,
  getNowString,
  inDateRange,
  normalizeKeyword,
  paginate,
  success,
  toWarningVO,
  withDelay,
} from "@/services/mock/helpers";

function listWarnings(query: {
  warningCode: string;
  warningLevel: WarningVO["warningLevel"] | "";
  warningStatus: WarningVO["warningStatus"] | "";
  startTime: string;
  endTime: string;
}) {
  const codeKeyword = normalizeKeyword(query.warningCode);
  return getMockDb().warnings
    .filter((item) => !codeKeyword || item.warningCode.toLowerCase().includes(codeKeyword))
    .filter((item) => !query.warningLevel || item.warningLevel === query.warningLevel)
    .filter((item) => query.warningStatus === "" || item.warningStatus === query.warningStatus)
    .filter((item) => inDateRange(item.createTime, query.startTime, query.endTime))
    .sort((left, right) => right.createTime.localeCompare(left.createTime))
    .map(toWarningVO);
}

export const mockWarningService: WarningService = {
  async pageWarnings(query) {
    const currentUser = ensureRole(["RISK_USER", "MANAGER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const records = listWarnings(query);
    return withDelay(success(paginate(records, query.pageNum, query.pageSize)));
  },
  async getWarningDetail(id) {
    const currentUser = ensureRole(["RISK_USER", "MANAGER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const warning = getMockDb().warnings.find((item) => item.id === id);
    if (!warning) {
      return withDelay(failure(40400, "未找到对应的预警记录", undefined as never));
    }

    const detail: WarningDetailVO = {
      id: warning.id,
      assessmentId: warning.assessmentId,
      riskDataId: warning.riskDataId,
      warningCode: warning.warningCode,
      warningLevel: warning.warningLevel,
      warningContent: warning.warningContent,
      businessNo: warning.businessNo,
      customerName: warning.customerName,
      warningStatus: warning.warningStatus,
      createTime: warning.createTime,
      businessType: warning.businessType,
      riskDesc: warning.riskDesc,
      totalScore: warning.totalScore,
      riskLevel: warning.riskLevel,
    };

    return withDelay(success(detail));
  },
  async listWarningRecords(id) {
    const currentUser = ensureRole(["RISK_USER", "MANAGER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const warning = getMockDb().warnings.find((item) => item.id === id);
    if (!warning) {
      return withDelay(failure(40400, "未找到对应的预警记录", [] as WarningHandleRecordVO[]));
    }

    return withDelay(success([...warning.handleRecords].sort((left, right) => right.handleTime.localeCompare(left.handleTime))));
  },
  async handleWarning(id, payload) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    if (!payload.handleOpinion.trim() || !payload.handleResult.trim()) {
      return withDelay(failure(40000, "处理意见和处理结果不能为空", undefined as never));
    }

    const warning = getMockDb().warnings.find((item) => item.id === id);
    if (!warning) {
      return withDelay(failure(40400, "未找到对应的预警记录", undefined as never));
    }
    if (warning.warningStatus === 2) {
      return withDelay(failure(42006, "该预警已处理完成，不能重复提交", undefined as never));
    }

    updateMockDb((draft) => {
      const current = draft.warnings.find((item) => item.id === id);
      if (!current) {
        return draft;
      }

      const record: WarningHandleRecordVO = {
        id: nextId(draft, "warningRecord"),
        warningId: id,
        handleUserId: currentUser.userId,
        handleUserName: currentUser.realName,
        handleOpinion: payload.handleOpinion.trim(),
        handleResult: payload.handleResult.trim(),
        nextStatus: payload.nextStatus,
        handleTime: getNowString(),
      };

      current.warningStatus = payload.nextStatus;
      current.handleRecords.unshift(record);

      const assessment = draft.assessments.find((item) => item.id === current.assessmentId);
      if (assessment?.warningInfo) {
        assessment.warningInfo.warningStatus = payload.nextStatus;
      }

      return draft;
    });

    return withDelay(success(undefined, "预警处理已保存"));
  },
};
