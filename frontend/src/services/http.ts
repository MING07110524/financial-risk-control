import { fetchCurrentUserApi, loginApi, logoutApi } from "@/api/auth";
import { actionAssistantApi, queryAssistantApi } from "@/api/assistant";
import { pageLogsApi } from "@/api/log";
import {
  executeAssessmentApi,
  getAssessmentDetailApi,
  pageAssessmentsApi,
} from "@/api/assessment";
import {
  createRiskIndexApi,
  createRiskRuleApi,
  createRiskDataApi,
  deleteRiskDataApi,
  deleteRiskRuleApi,
  getRiskDataDetailApi,
  listRiskIndexesApi,
  listRiskRulesApi,
  updateRiskIndexApi,
  updateRiskIndexStatusApi,
  updateRiskRuleApi,
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
import {
  createUserApi,
  deleteUserApi,
  getUserByIdApi,
  listRolesApi,
  pageUsersApi,
  updateUserApi,
  updateUserStatusApi,
} from "@/api/system";
import type { Result } from "@/types/common";
import type { LogService, ServiceBundle, UserService } from "@/services/contracts";

function notImplemented<T>(message = "当前未启用 Mock，且真实后端适配器尚未补齐"): Promise<Result<T>> {
  return Promise.resolve({
    code: 50100,
    message,
    data: undefined as T,
  });
}

const userService: UserService = {
  pageUsers: pageUsersApi,
  getUserById: getUserByIdApi,
  createUser: createUserApi,
  updateUser: updateUserApi,
  updateUserStatus: updateUserStatusApi,
  deleteUser: deleteUserApi,
  listRoles: listRolesApi,
};

const logService: LogService = {
  pageLogs: pageLogsApi,
};

export function createHttpServiceBundle(): ServiceBundle {
  return {
    authService: {
      login: loginApi,
      logout: logoutApi,
      getCurrentUser: fetchCurrentUserApi,
    },
    userService,
    logService,
    dashboardService: {
      getDashboardStatistics: getDashboardStatisticsApi,
      listRecentWarnings: listRecentWarningsApi,
    },
    riskIndexService: {
      listRiskIndexes: listRiskIndexesApi,
      createRiskIndex: createRiskIndexApi,
      updateRiskIndex: updateRiskIndexApi,
      updateRiskIndexStatus: updateRiskIndexStatusApi,
      listRiskRules: listRiskRulesApi,
      createRiskRule: createRiskRuleApi,
      updateRiskRule: updateRiskRuleApi,
      deleteRiskRule: deleteRiskRuleApi,
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
    assistantService: {
      query: queryAssistantApi,
      action: actionAssistantApi,
    },
    systemService: {
      resetDemoData: () => notImplemented("真实环境下不支持重置演示数据"),
    },
  };
}
