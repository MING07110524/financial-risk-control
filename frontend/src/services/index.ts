import { createHttpServiceBundle } from "@/services/http";
import { createMockServiceBundle } from "@/services/mock";

const isMockMode = import.meta.env.VITE_USE_MOCK === "true";
const bundle = isMockMode ? createMockServiceBundle() : createHttpServiceBundle();

export const authService = bundle.authService;
export const userService = bundle.userService;
export const logService = bundle.logService;
export const dashboardService = bundle.dashboardService;
export const riskIndexService = bundle.riskIndexService;
export const riskDataService = bundle.riskDataService;
export const assessmentService = bundle.assessmentService;
export const warningService = bundle.warningService;
export const statisticsService = bundle.statisticsService;
export const assistantService = bundle.assistantService;
export const systemService = bundle.systemService;
export { isMockMode };
