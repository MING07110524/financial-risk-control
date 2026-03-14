import type { CurrentUser, LoginRequest, LoginUser } from "@/types/auth";
import type { PageResult, Result } from "@/types/common";
import type { LogVO, RoleVO, UserVO } from "@/types/system";
import type {
  AssessmentDetailVO,
  AssessmentQuery,
  AssessmentVO,
  DashboardStatisticsVO,
  HandleSummaryStatisticsVO,
  RiskIndexCreateDTO,
  RiskDataCreateDTO,
  RiskDataDetailVO,
  RiskDataQuery,
  RiskIndexStatusDTO,
  RiskIndexUpdateDTO,
  RiskDataVO,
  RiskIndexQuery,
  RiskIndexVO,
  RiskLevelStatisticsVO,
  RiskRuleCreateDTO,
  RiskRuleUpdateDTO,
  RiskRuleVO,
  StatisticsQuery,
  WarningDetailVO,
  WarningHandleDTO,
  WarningHandleRecordVO,
  WarningQuery,
  WarningTrendStatisticsVO,
  WarningVO,
} from "@/types/risk";

export interface UserQuery {
  username?: string;
  realName?: string;
  roleCode?: string;
  status?: number | "";
}

export interface AuthService {
  login(payload: LoginRequest): Promise<Result<LoginUser>>;
  logout(): Promise<Result<void>>;
  getCurrentUser(): Promise<Result<CurrentUser>>;
}

export interface UserService {
  pageUsers(query: UserQuery, pageNum: number, pageSize: number): Promise<Result<PageResult<UserVO>>>;
  getUserById(id: number): Promise<Result<UserVO>>;
  createUser(payload: { username: string; password: string; realName: string; phone?: string; roleIds: number[] }): Promise<Result<UserVO>>;
  updateUser(id: number, payload: { username?: string; realName?: string; phone?: string; roleIds?: number[] }): Promise<Result<UserVO>>;
  updateUserStatus(id: number, status: number): Promise<Result<UserVO>>;
  deleteUser(id: number): Promise<Result<void>>;
  listRoles(): Promise<Result<RoleVO[]>>;
}

export interface LogQuery {
  moduleName?: string;
  operationType?: string;
  operator?: string;
  startTime?: string;
  endTime?: string;
}

export interface LogService {
  pageLogs(query: LogQuery, pageNum: number, pageSize: number): Promise<Result<PageResult<LogVO>>>;
}

export interface DashboardService {
  getDashboardStatistics(): Promise<Result<DashboardStatisticsVO>>;
  listRecentWarnings(): Promise<Result<WarningVO[]>>;
}

export interface RiskIndexService {
  listRiskIndexes(query?: RiskIndexQuery): Promise<Result<RiskIndexVO[]>>;
  createRiskIndex(payload: RiskIndexCreateDTO): Promise<Result<RiskIndexVO>>;
  updateRiskIndex(id: number, payload: RiskIndexUpdateDTO): Promise<Result<RiskIndexVO>>;
  updateRiskIndexStatus(id: number, payload: RiskIndexStatusDTO): Promise<Result<RiskIndexVO>>;
  listRiskRules(indexId: number): Promise<Result<RiskRuleVO[]>>;
  createRiskRule(payload: RiskRuleCreateDTO): Promise<Result<RiskRuleVO>>;
  updateRiskRule(id: number, payload: RiskRuleUpdateDTO): Promise<Result<RiskRuleVO>>;
  deleteRiskRule(id: number): Promise<Result<void>>;
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

export interface AssistantService {
  query(payload: Record<string, unknown>): Promise<Result<void>>;
  action(payload: Record<string, unknown>): Promise<Result<void>>;
}

export interface SystemService {
  resetDemoData(): Promise<Result<void>>;
}

export interface ServiceBundle {
  authService: AuthService;
  userService: UserService;
  logService: LogService;
  dashboardService: DashboardService;
  riskIndexService: RiskIndexService;
  riskDataService: RiskDataService;
  assessmentService: AssessmentService;
  warningService: WarningService;
  statisticsService: StatisticsService;
  assistantService: AssistantService;
  systemService: SystemService;
}
