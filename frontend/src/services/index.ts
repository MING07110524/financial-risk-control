import { createHttpServiceBundle } from "@/services/http";
import { createMockServiceBundle } from "@/services/mock";

const useMock = import.meta.env.VITE_USE_MOCK !== "false";
const useHttpAuth = import.meta.env.VITE_AUTH_USE_HTTP === "true";
const useHttpRiskData = import.meta.env.VITE_RISK_DATA_USE_HTTP === "true";
const useHttpRiskIndex = import.meta.env.VITE_RISK_INDEX_USE_HTTP === "true";
const useHttpDashboard = import.meta.env.VITE_DASHBOARD_USE_HTTP === "true";
const useHttpAssessment = import.meta.env.VITE_ASSESSMENT_USE_HTTP === "true";
const useHttpWarning = import.meta.env.VITE_WARNING_USE_HTTP === "true";
const useHttpStatistics = import.meta.env.VITE_STATISTICS_USE_HTTP === "true";
const mockBundle = createMockServiceBundle();
const httpBundle = createHttpServiceBundle();
// Keep business modules on mock while only authentication talks to the real
// backend. This lets us validate JWT + route guards first, without forcing the
// whole business chain to switch at the same time. / 当前先让“认证走真实后端，
// 业务仍走 mock”，这样可以先验证 JWT 和路由守卫，而不用把整条业务链一次切真。
const bundle = useMock ? mockBundle : httpBundle;

export const authService = useHttpAuth ? httpBundle.authService : bundle.authService;
export const dashboardService = useHttpDashboard ? httpBundle.dashboardService : bundle.dashboardService;
export const riskIndexService = useHttpRiskIndex ? httpBundle.riskIndexService : bundle.riskIndexService;
export const riskDataService = useHttpRiskData ? httpBundle.riskDataService : bundle.riskDataService;
export const assessmentService = useHttpAssessment ? httpBundle.assessmentService : bundle.assessmentService;
export const warningService = useHttpWarning ? httpBundle.warningService : bundle.warningService;
export const statisticsService = useHttpStatistics ? httpBundle.statisticsService : bundle.statisticsService;
export const systemService = bundle.systemService;
export const isMockMode = useMock;
export const isAuthMockMode = !useHttpAuth;
export const isRiskDataHttpMode = useHttpRiskData;
export const isRiskIndexHttpMode = useHttpRiskIndex;
export const isDashboardHttpMode = useHttpDashboard;
export const isAssessmentHttpMode = useHttpAssessment;
export const isWarningHttpMode = useHttpWarning;
export const isStatisticsHttpMode = useHttpStatistics;
export const isCoreWorkflowMockMode =
  !useHttpRiskData || !useHttpRiskIndex || !useHttpDashboard || !useHttpAssessment || !useHttpWarning || !useHttpStatistics;
