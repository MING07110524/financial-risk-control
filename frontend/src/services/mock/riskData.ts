import type { RiskDataService } from "@/services/contracts";
import type { RiskDataCreateDTO, RiskDataDetailVO, RiskDataVO } from "@/types/risk";
import { getMockDb, nextId, updateMockDb } from "@/mock/db";
import {
  ensureRole,
  failure,
  getNowString,
  normalizeKeyword,
  paginate,
  success,
  validateIndexValues,
  withDelay,
} from "@/services/mock/helpers";

function createRiskDataDetail(
  db: ReturnType<typeof getMockDb>,
  id: number,
  payload: RiskDataCreateDTO,
  currentUser: { userId: number; realName: string },
  createTime: string,
  updateTime: string,
  dataStatus: 0 | 1 | 2,
): RiskDataDetailVO {
  return {
    id,
    businessNo: payload.businessNo.trim(),
    customerName: payload.customerName.trim(),
    businessType: payload.businessType.trim(),
    riskDesc: payload.riskDesc.trim(),
    dataStatus,
    createBy: currentUser.userId,
    createByName: currentUser.realName,
    createTime,
    updateTime,
    indexValues: payload.indexValues.map((item) => {
      const index = db.riskIndexes.find((entry) => entry.id === item.indexId);
      if (!index || item.indexValue === null) {
        throw new Error("Index value data is invalid");
      }
      return {
        indexId: index.id,
        indexCode: index.indexCode,
        indexName: index.indexName,
        indexValue: item.indexValue,
        weightValue: index.weightValue,
      };
    }),
  };
}

function toRiskDataVO(item: RiskDataDetailVO): RiskDataVO {
  return {
    id: item.id,
    businessNo: item.businessNo,
    customerName: item.customerName,
    businessType: item.businessType,
    riskDesc: item.riskDesc,
    dataStatus: item.dataStatus,
    createBy: item.createBy,
    createByName: item.createByName,
    createTime: item.createTime,
    updateTime: item.updateTime,
  };
}

export const mockRiskDataService: RiskDataService = {
  async pageRiskData(query) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const businessKeyword = normalizeKeyword(query.businessNo);
    const customerKeyword = normalizeKeyword(query.customerName);
    const typeKeyword = normalizeKeyword(query.businessType);
    const records = getMockDb().riskData
      .filter((item) => !businessKeyword || item.businessNo.toLowerCase().includes(businessKeyword))
      .filter((item) => !customerKeyword || item.customerName.toLowerCase().includes(customerKeyword))
      .filter((item) => !typeKeyword || item.businessType.toLowerCase().includes(typeKeyword))
      .filter((item) => query.dataStatus === "" || item.dataStatus === query.dataStatus)
      .sort((left, right) => right.createTime.localeCompare(left.createTime))
      .map(toRiskDataVO);

    return withDelay(success(paginate(records, query.pageNum, query.pageSize)));
  },
  async getRiskDataDetail(id) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const item = getMockDb().riskData.find((entry) => entry.id === id);
    if (!item) {
      return withDelay(failure(40400, "未找到对应的风险数据", undefined as never));
    }

    return withDelay(success(item));
  },
  async createRiskData(payload) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const db = getMockDb();
    if (db.riskData.some((item) => item.businessNo === payload.businessNo.trim())) {
      return withDelay(failure(42001, "业务编号已存在，请更换后重试", undefined as never));
    }

    const validation = validateIndexValues(db, payload);
    if (validation) {
      return withDelay(validation);
    }

    const now = getNowString();
    const nextDb = updateMockDb((draft) => {
      const id = nextId(draft, "riskData");
      const detail = createRiskDataDetail(draft, id, payload, currentUser, now, now, 0);
      draft.riskData.unshift(detail);
      return draft;
    });

    const created = nextDb.riskData.find((item) => item.businessNo === payload.businessNo.trim());
    if (!created) {
      return withDelay(failure(50000, "创建风险数据失败，请稍后再试", undefined as never));
    }

    return withDelay(success(created, "保存成功"));
  },
  async updateRiskData(id, payload) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const db = getMockDb();
    const existed = db.riskData.find((item) => item.id === id);
    if (!existed) {
      return withDelay(failure(40400, "未找到对应的风险数据", undefined as never));
    }

    const validation = validateIndexValues(db, payload);
    if (validation) {
      return withDelay(validation);
    }

    const now = getNowString();
    const nextDb = updateMockDb((draft) => {
      const current = draft.riskData.find((item) => item.id === id);
      if (!current) {
        return draft;
      }

      current.customerName = payload.customerName.trim();
      current.businessType = payload.businessType.trim();
      current.riskDesc = payload.riskDesc.trim();
      current.updateTime = now;
      current.indexValues = payload.indexValues.map((item) => {
        const index = draft.riskIndexes.find((entry) => entry.id === item.indexId);
        if (!index || item.indexValue === null) {
          throw new Error("Index value data is invalid");
        }
        return {
          indexId: index.id,
          indexCode: index.indexCode,
          indexName: index.indexName,
          indexValue: item.indexValue,
          weightValue: index.weightValue,
        };
      });

      const activeAssessment = draft.assessments.find((item) => item.riskDataId === id && item.assessmentStatus === 1);
      if (activeAssessment) {
        activeAssessment.assessmentStatus = 0;
        current.dataStatus = 2;
      }

      return draft;
    });

    const updated = nextDb.riskData.find((item) => item.id === id);
    if (!updated) {
      return withDelay(failure(50000, "更新风险数据失败，请稍后再试", undefined as never));
    }

    return withDelay(success(updated, "更新成功"));
  },
  async deleteRiskData(id) {
    const currentUser = ensureRole(["RISK_USER"]);
    if ("code" in currentUser) {
      return withDelay(currentUser);
    }

    const db = getMockDb();
    const existed = db.riskData.find((item) => item.id === id);
    if (!existed) {
      return withDelay(failure(40400, "未找到对应的风险数据", undefined as never));
    }

    if (db.assessments.some((item) => item.riskDataId === id) || db.warnings.some((item) => item.riskDataId === id)) {
      return withDelay(failure(40900, "该业务数据已有评估或预警记录，当前演示版不允许删除", undefined as never));
    }

    updateMockDb((draft) => {
      draft.riskData = draft.riskData.filter((item) => item.id !== id);
      return draft;
    });

    return withDelay(success(undefined, "删除成功"));
  },
};
