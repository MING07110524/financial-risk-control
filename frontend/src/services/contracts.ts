import type { CurrentUser, LoginRequest, LoginUser } from "@/types/auth";
import type { PageResult, Result } from "@/types/common";
import type {
  AssessmentDetailVO,
  AssessmentQuery,
  AssessmentVO,
  DashboardStatisticsVO,
  HandleSummaryStatisticsVO,
  RiskDataCreateDTO,
  RiskDataDetailVO,
  RiskDataQuery,
  RiskDataVO,
  RiskIndexQuery,
  RiskIndexVO,
  RiskLevelStatisticsVO,
  RiskRuleVO,
  StatisticsQuery,
  WarningDetailVO,
  WarningHandleDTO,
  WarningHandleRecordVO,
  WarningQuery,
  WarningTrendStatisticsVO,
  WarningVO,
} from "@/types/risk";

export interface AuthService {
  login(payload: LoginRequest): Promise<Result<LoginUser>>;
  logout(): Promise<Result<void>>;
  getCurrentUser(): Promise<Result<CurrentUser>>;
}

export interface DashboardService {
  getDashboardStatistics(): Promise<Result<DashboardStatisticsVO>>;
  listRecentWarnings(): Promise<Result<WarningVO[]>>;
}

export interface RiskIndexService {
  listRiskIndexes(query?: RiskIndexQuery): Promise<Result<RiskIndexVO[]>>;
  listRiskRules(indexId: number): Promise<Result<RiskRuleVO[]>>;
}

export interface RiskDataService {
  pageRiskData(query: RiskDataQuery): Promise<Result<PageResult<RiskDataVO>>>;
  getRiskDataDetail(id: number): Promise<Result<RiskDataDetailVO>>;
  createRiskData(payload: RiskDataCreateDTO): Promise<Result<RiskDataDetailVO>>;
  updateRiskData(id: number, payload: RiskDataCreateDTO): Promise<Result<RiskDataDetailVO>>;
  deleteRiskData(id: number): Promise<Result<void>>;
}

export interface AssessmentService {
  pageAssessments(query: AssessmentQuery): Promise<Result<PageResult<AssessmentVO>>>;
  getAssessmentDetail(id: number): Promise<Result<AssessmentDetailVO>>;
  executeAssessment(riskDataId: number): Promise<Result<AssessmentDetailVO>>;
}

export interface WarningService {
  pageWarnings(query: WarningQuery): Promise<Result<PageResult<WarningVO>>>;
  getWarningDetail(id: number): Promise<Result<WarningDetailVO>>;
  listWarningRecords(id: number): Promise<Result<WarningHandleRecordVO[]>>;
  handleWarning(id: number, payload: WarningHandleDTO): Promise<Result<void>>;
}

export interface StatisticsService {
  getRiskLevelStatistics(query: StatisticsQuery): Promise<Result<RiskLevelStatisticsVO[]>>;
  getWarningTrendStatistics(query: StatisticsQuery): Promise<Result<WarningTrendStatisticsVO[]>>;
  getHandleSummaryStatistics(query: StatisticsQuery): Promise<Result<HandleSummaryStatisticsVO[]>>;
}

export interface SystemService {
  resetDemoData(): Promise<Result<void>>;
}

export interface ServiceBundle {
  authService: AuthService;
  dashboardService: DashboardService;
  riskIndexService: RiskIndexService;
  riskDataService: RiskDataService;
  assessmentService: AssessmentService;
  warningService: WarningService;
  statisticsService: StatisticsService;
  systemService: SystemService;
}
