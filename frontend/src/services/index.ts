import { createHttpServiceBundle } from "@/services/http";
import { createMockServiceBundle } from "@/services/mock";

const useMock = import.meta.env.VITE_USE_MOCK !== "false";
const bundle = useMock ? createMockServiceBundle() : createHttpServiceBundle();

export const authService = bundle.authService;
export const dashboardService = bundle.dashboardService;
export const riskIndexService = bundle.riskIndexService;
export const riskDataService = bundle.riskDataService;
export const assessmentService = bundle.assessmentService;
export const warningService = bundle.warningService;
export const statisticsService = bundle.statisticsService;
export const systemService = bundle.systemService;
export const isMockMode = useMock;
