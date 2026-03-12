import { createHttpServiceBundle } from "@/services/http";
import { createMockServiceBundle } from "@/services/mock";

const useMock = import.meta.env.VITE_USE_MOCK !== "false";
const useHttpAuth = import.meta.env.VITE_AUTH_USE_HTTP === "true";
const mockBundle = createMockServiceBundle();
const httpBundle = createHttpServiceBundle();
// Keep business modules on mock while only authentication talks to the real
// backend. This lets us validate JWT + route guards first, without forcing the
// whole business chain to switch at the same time. / 当前先让“认证走真实后端，
// 业务仍走 mock”，这样可以先验证 JWT 和路由守卫，而不用把整条业务链一次切真。
const bundle = useMock ? mockBundle : httpBundle;

export const authService = useHttpAuth ? httpBundle.authService : bundle.authService;
export const dashboardService = bundle.dashboardService;
export const riskIndexService = bundle.riskIndexService;
export const riskDataService = bundle.riskDataService;
export const assessmentService = bundle.assessmentService;
export const warningService = bundle.warningService;
export const statisticsService = bundle.statisticsService;
export const systemService = bundle.systemService;
export const isMockMode = useMock;
export const isAuthMockMode = !useHttpAuth;
