import type { ServiceBundle } from "@/services/contracts";
import { mockAssessmentService } from "@/services/mock/assessment";
import { mockAuthService } from "@/services/mock/auth";
import { mockDashboardService } from "@/services/mock/dashboard";
import { mockRiskDataService } from "@/services/mock/riskData";
import { mockRiskIndexService } from "@/services/mock/riskIndex";
import { mockStatisticsService } from "@/services/mock/statistics";
import { mockSystemService } from "@/services/mock/system";
import { mockWarningService } from "@/services/mock/warning";

export function createMockServiceBundle(): ServiceBundle {
  return {
    authService: mockAuthService,
    dashboardService: mockDashboardService,
    riskIndexService: mockRiskIndexService,
    riskDataService: mockRiskDataService,
    assessmentService: mockAssessmentService,
    warningService: mockWarningService,
    statisticsService: mockStatisticsService,
    systemService: mockSystemService,
  };
}
