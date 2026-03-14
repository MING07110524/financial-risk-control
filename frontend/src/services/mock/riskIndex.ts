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
  async createRiskIndex() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持指标写操作", data: undefined as never });
  },
  async updateRiskIndex() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持指标写操作", data: undefined as never });
  },
  async updateRiskIndexStatus() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持指标写操作", data: undefined as never });
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
  async createRiskRule() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持规则写操作", data: undefined as never });
  },
  async updateRiskRule() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持规则写操作", data: undefined as never });
  },
  async deleteRiskRule() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持规则写操作", data: undefined as never });
  },
};
