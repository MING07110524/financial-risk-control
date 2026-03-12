import { fetchCurrentUserApi, loginApi, logoutApi } from "@/api/auth";
import {
  executeAssessmentApi,
  getAssessmentDetailApi,
  pageAssessmentsApi,
} from "@/api/assessment";
import {
  createRiskDataApi,
  deleteRiskDataApi,
  getRiskDataDetailApi,
  listRiskIndexesApi,
  listRiskRulesApi,
  pageRiskDataApi,
  updateRiskDataApi,
} from "@/api/risk";
import {
  getDashboardStatisticsApi,
  getHandleSummaryStatisticsApi,
  getRiskLevelStatisticsApi,
  getWarningTrendStatisticsApi,
  listRecentWarningsApi,
} from "@/api/statistics";
import {
  getWarningDetailApi,
  handleWarningApi,
  listWarningRecordsApi,
  pageWarningsApi,
} from "@/api/warning";
import type { Result } from "@/types/common";
import type { ServiceBundle } from "@/services/contracts";

function notImplemented<T>(message = "当前未启用 Mock，且真实后端适配器尚未补齐"): Promise<Result<T>> {
  return Promise.resolve({
    code: 50100,
    message,
    data: undefined as T,
  });
}

export function createHttpServiceBundle(): ServiceBundle {
  return {
    authService: {
      login: loginApi,
      logout: logoutApi,
      getCurrentUser: fetchCurrentUserApi,
    },
    dashboardService: {
      getDashboardStatistics: getDashboardStatisticsApi,
      listRecentWarnings: listRecentWarningsApi,
    },
    riskIndexService: {
      listRiskIndexes: listRiskIndexesApi,
      listRiskRules: listRiskRulesApi,
    },
    riskDataService: {
      pageRiskData: pageRiskDataApi,
      getRiskDataDetail: getRiskDataDetailApi,
      createRiskData: createRiskDataApi,
      updateRiskData: updateRiskDataApi,
      deleteRiskData: deleteRiskDataApi,
    },
    assessmentService: {
      pageAssessments: pageAssessmentsApi,
      getAssessmentDetail: getAssessmentDetailApi,
      executeAssessment: executeAssessmentApi,
    },
    warningService: {
      pageWarnings: pageWarningsApi,
      getWarningDetail: getWarningDetailApi,
      listWarningRecords: listWarningRecordsApi,
      handleWarning: handleWarningApi,
    },
    statisticsService: {
      getRiskLevelStatistics: getRiskLevelStatisticsApi,
      getWarningTrendStatistics: getWarningTrendStatisticsApi,
      getHandleSummaryStatistics: getHandleSummaryStatisticsApi,
    },
    systemService: {
      resetDemoData: () => notImplemented("真实环境下不支持重置演示数据"),
    },
  };
}
