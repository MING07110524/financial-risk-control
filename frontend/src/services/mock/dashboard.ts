import type { DashboardService } from "@/services/contracts";
import { buildDashboardBundle, getMockDb } from "@/mock/db";
import { ensureAuthenticated, success, withDelay } from "@/services/mock/helpers";

export const mockDashboardService: DashboardService = {
  async getDashboardStatistics() {
    const currentUser = ensureAuthenticated();
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }
    return withDelay(success(buildDashboardBundle(getMockDb()).dashboard));
  },
  async listRecentWarnings() {
    const currentUser = ensureAuthenticated();
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }
    return withDelay(success(buildDashboardBundle(getMockDb()).recentWarnings));
  },
};
