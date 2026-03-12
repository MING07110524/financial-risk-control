import type { CurrentUser } from "@/types/auth";
import type { PageResult, Result } from "@/types/common";
import type {
  RiskDataCreateDTO,
  RiskIndexVO,
  StatisticsQuery,
  WarningVO,
} from "@/types/risk";
import type { MockAssessment, MockDb, MockWarning } from "@/mock/schema";
import { getCurrentMockUser } from "@/mock/db";

export function success<T>(data: T, message = "success"): Result<T> {
  return {
    code: 0,
    message,
    data,
  };
}

export function failure<T>(code: number, message: string, data: T): Result<T> {
  return {
    code,
    message,
    data,
  };
}

export function withDelay<T>(result: Result<T>, delay = 140): Promise<Result<T>> {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(result), delay);
  });
}

export function ensureAuthenticated(): CurrentUser | Result<never> {
  const currentUser = getCurrentMockUser();
  if (!currentUser) {
    return failure(40100, "请先登录后再继续操作", undefined as never);
  }
  return currentUser;
}

export function ensureRole(roles: CurrentUser["roleCode"][]): CurrentUser | Result<never> {
  const currentUser = ensureAuthenticated();
  if ("code" in currentUser) {
    return currentUser;
  }
  if (!roles.includes(currentUser.roleCode)) {
    return failure(40300, "当前账号没有权限访问该功能", undefined as never);
  }
  return currentUser;
}

export function normalizeKeyword(value: string): string {
  return value.trim().toLowerCase();
}

export function inDateRange(value: string, startTime: string, endTime: string): boolean {
  const date = value.slice(0, 10);
  const start = startTime ? startTime.slice(0, 10) : "";
  const end = endTime ? endTime.slice(0, 10) : "";

  if (start && date < start) {
    return false;
  }
  if (end && date > end) {
    return false;
  }
  return true;
}

export function paginate<T>(records: T[], pageNum: number, pageSize: number): PageResult<T> {
  const start = (pageNum - 1) * pageSize;
  return {
    total: records.length,
    records: records.slice(start, start + pageSize),
  };
}

export function listEnabledIndexes(db: MockDb): RiskIndexVO[] {
  return db.riskIndexes.filter((item) => item.status === 1);
}

export function validateIndexValues(db: MockDb, payload: RiskDataCreateDTO): Result<never> | null {
  const enabledIndexes = listEnabledIndexes(db);
  const enabledIds = enabledIndexes.map((item) => item.id).sort((left, right) => left - right);
  const providedIds = payload.indexValues
    .filter((item) => item.indexValue !== null)
    .map((item) => item.indexId)
    .sort((left, right) => left - right);

  if (
    enabledIds.length !== providedIds.length ||
    !enabledIds.every((id, index) => id === providedIds[index])
  ) {
    return failure(42004, "所有启用指标都必须填写且只能填写一次", undefined as never);
  }

  return null;
}

export function getNowString(): string {
  const date = new Date();
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  const hours = `${date.getHours()}`.padStart(2, "0");
  const minutes = `${date.getMinutes()}`.padStart(2, "0");
  const seconds = `${date.getSeconds()}`.padStart(2, "0");

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

export function roundScore(value: number): number {
  return Number(value.toFixed(2));
}

export function toWarningVO(item: MockWarning): WarningVO {
  return {
    id: item.id,
    assessmentId: item.assessmentId,
    riskDataId: item.riskDataId,
    warningCode: item.warningCode,
    warningLevel: item.warningLevel,
    warningContent: item.warningContent,
    businessNo: item.businessNo,
    customerName: item.customerName,
    warningStatus: item.warningStatus,
    createTime: item.createTime,
  };
}

export function filterStatisticsWarnings(db: MockDb, query: StatisticsQuery): MockWarning[] {
  return db.warnings
    .filter((item) => !query.warningStatus || item.warningStatus === query.warningStatus)
    .filter((item) => !query.riskLevel || item.riskLevel === query.riskLevel)
    .filter((item) => inDateRange(item.createTime, query.startTime, query.endTime));
}

export function filterStatisticsAssessments(db: MockDb, query: StatisticsQuery): MockAssessment[] {
  return db.assessments
    .filter((item) => item.assessmentStatus === 1)
    .filter((item) => !query.riskLevel || item.riskLevel === query.riskLevel)
    .filter((item) => inDateRange(item.assessmentTime, query.startTime, query.endTime));
}
