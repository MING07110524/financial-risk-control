import type { StatisticsService } from "@/services/contracts";
import type { HandleSummaryStatisticsVO, RiskLevelStatisticsVO, WarningTrendStatisticsVO } from "@/types/risk";
import { getMockDb } from "@/mock/db";
import {
  ensureRole,
  filterStatisticsAssessments,
  filterStatisticsWarnings,
  success,
  withDelay,
} from "@/services/mock/helpers";

export const mockStatisticsService: StatisticsService = {
  async getRiskLevelStatistics(query) {
    const currentUser = ensureRole(["RISK_USER", "MANAGER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const assessments = filterStatisticsAssessments(getMockDb(), query);
    const records: RiskLevelStatisticsVO[] = [
      { riskLevel: "LOW", count: assessments.filter((item) => item.riskLevel === "LOW").length },
      { riskLevel: "MEDIUM", count: assessments.filter((item) => item.riskLevel === "MEDIUM").length },
      { riskLevel: "HIGH", count: assessments.filter((item) => item.riskLevel === "HIGH").length },
    ];
    return withDelay(success(records));
  },
  async getWarningTrendStatistics(query) {
    const currentUser = ensureRole(["RISK_USER", "MANAGER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const warnings = filterStatisticsWarnings(getMockDb(), query);
    const grouped = new Map<string, WarningTrendStatisticsVO>();
    warnings.forEach((item) => {
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

    return withDelay(success([...grouped.values()].sort((left, right) => left.date.localeCompare(right.date))));
  },
  async getHandleSummaryStatistics(query) {
    const currentUser = ensureRole(["RISK_USER", "MANAGER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const warnings = filterStatisticsWarnings(getMockDb(), query);
    const records: HandleSummaryStatisticsVO[] = [
      { warningStatus: 0, label: "待处理", count: warnings.filter((item) => item.warningStatus === 0).length },
      { warningStatus: 1, label: "处理中", count: warnings.filter((item) => item.warningStatus === 1).length },
      { warningStatus: 2, label: "已处理", count: warnings.filter((item) => item.warningStatus === 2).length },
    ];
    return withDelay(success(records));
  },
};
