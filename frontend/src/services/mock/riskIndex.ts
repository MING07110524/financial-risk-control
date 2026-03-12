import type { RiskIndexService } from "@/services/contracts";
import { getMockDb } from "@/mock/db";
import { ensureRole, normalizeKeyword, success, withDelay } from "@/services/mock/helpers";

export const mockRiskIndexService: RiskIndexService = {
  async listRiskIndexes(query) {
    const currentUser = ensureRole(["ADMIN", "RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const keyword = normalizeKeyword(query?.indexName ?? "");
    const records = getMockDb().riskIndexes
      .filter((item) => query?.status === undefined || item.status === query.status)
      .filter((item) => !keyword || item.indexName.toLowerCase().includes(keyword));

    return withDelay(success(records));
  },
  async listRiskRules(indexId) {
    const currentUser = ensureRole(["ADMIN"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const records = getMockDb().riskRules
      .filter((item) => item.indexId === indexId)
      .sort((left, right) => left.scoreMin - right.scoreMin);

    return withDelay(success(records));
  },
};
