import client from "@/api/client";
import type { PageResult, Result } from "@/types/common";
import type {
  RiskDataCreateDTO,
  RiskDataDetailVO,
  RiskDataQuery,
  RiskDataVO,
  RiskIndexQuery,
  RiskIndexVO,
  RiskRuleVO,
} from "@/types/risk";

export async function pageRiskDataApi(query: RiskDataQuery): Promise<Result<PageResult<RiskDataVO>>> {
  const { data } = await client.get<Result<PageResult<RiskDataVO>>>("/risk-data", {
    params: query,
  });
  return data;
}

export async function getRiskDataDetailApi(id: number): Promise<Result<RiskDataDetailVO>> {
  const { data } = await client.get<Result<RiskDataDetailVO>>(`/risk-data/${id}`);
  return data;
}

export async function createRiskDataApi(payload: RiskDataCreateDTO): Promise<Result<RiskDataDetailVO>> {
  const { data } = await client.post<Result<RiskDataDetailVO>>("/risk-data", payload);
  return data;
}

export async function updateRiskDataApi(id: number, payload: RiskDataCreateDTO): Promise<Result<RiskDataDetailVO>> {
  const { data } = await client.put<Result<RiskDataDetailVO>>(`/risk-data/${id}`, payload);
  return data;
}

export async function deleteRiskDataApi(id: number): Promise<Result<void>> {
  const { data } = await client.delete<Result<void>>(`/risk-data/${id}`);
  return data;
}

export async function listRiskIndexesApi(query?: RiskIndexQuery): Promise<Result<RiskIndexVO[]>> {
  const { data } = await client.get<Result<RiskIndexVO[]>>("/risk-indexes", {
    params: query,
  });
  return data;
}

export async function listRiskRulesApi(indexId: number): Promise<Result<RiskRuleVO[]>> {
  const { data } = await client.get<Result<RiskRuleVO[]>>("/risk-rules", {
    params: { indexId },
  });
  return data;
}
