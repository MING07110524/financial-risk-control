import { fetchCurrentUserApi, loginApi, logoutApi } from "@/api/auth";
import {
  createRiskDataApi,
  deleteRiskDataApi,
  getRiskDataDetailApi,
  listRiskIndexesApi,
  listRiskRulesApi,
  pageRiskDataApi,
  updateRiskDataApi,
} from "@/api/risk";
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
      getDashboardStatistics: () => notImplemented(),
      listRecentWarnings: () => notImplemented(),
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
      pageAssessments: () => notImplemented(),
      getAssessmentDetail: () => notImplemented(),
      executeAssessment: () => notImplemented(),
    },
    warningService: {
      pageWarnings: () => notImplemented(),
      getWarningDetail: () => notImplemented(),
      listWarningRecords: () => notImplemented(),
      handleWarning: () => notImplemented(),
    },
    statisticsService: {
      getRiskLevelStatistics: () => notImplemented(),
      getWarningTrendStatistics: () => notImplemented(),
      getHandleSummaryStatistics: () => notImplemented(),
    },
    systemService: {
      resetDemoData: () => notImplemented("真实环境下不支持重置演示数据"),
    },
  };
}
