import client from "@/api/client";
import { compactParams } from "@/api/params";
import type { PageResult, Result } from "@/types/common";
import type {
  RiskIndexCreateDTO,
  RiskIndexStatusDTO,
  RiskIndexUpdateDTO,
  RiskDataCreateDTO,
  RiskDataDetailVO,
  RiskDataQuery,
  RiskDataVO,
  RiskIndexQuery,
  RiskIndexVO,
  RiskRuleCreateDTO,
  RiskRuleUpdateDTO,
  RiskRuleVO,
} from "@/types/risk";

export async function pageRiskDataApi(query: RiskDataQuery): Promise<Result<PageResult<RiskDataVO>>> {
  const { data } = await client.get<Result<PageResult<RiskDataVO>>>("/risk-data", {
    params: compactParams(query),
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
    params: query ? compactParams(query) : undefined,
  });
  return data;
}

export async function createRiskIndexApi(payload: RiskIndexCreateDTO): Promise<Result<RiskIndexVO>> {
  const { data } = await client.post<Result<RiskIndexVO>>("/risk-indexes", payload);
  return data;
}

export async function updateRiskIndexApi(id: number, payload: RiskIndexUpdateDTO): Promise<Result<RiskIndexVO>> {
  const { data } = await client.put<Result<RiskIndexVO>>(`/risk-indexes/${id}`, payload);
  return data;
}

export async function updateRiskIndexStatusApi(id: number, payload: RiskIndexStatusDTO): Promise<Result<RiskIndexVO>> {
  const { data } = await client.put<Result<RiskIndexVO>>(`/risk-indexes/${id}/status`, payload);
  return data;
}

export async function listRiskRulesApi(indexId: number): Promise<Result<RiskRuleVO[]>> {
  const { data } = await client.get<Result<RiskRuleVO[]>>("/risk-rules", {
    params: { indexId },
  });
  return data;
}

export async function createRiskRuleApi(payload: RiskRuleCreateDTO): Promise<Result<RiskRuleVO>> {
  const { data } = await client.post<Result<RiskRuleVO>>("/risk-rules", payload);
  return data;
}

export async function updateRiskRuleApi(id: number, payload: RiskRuleUpdateDTO): Promise<Result<RiskRuleVO>> {
  const { data } = await client.put<Result<RiskRuleVO>>(`/risk-rules/${id}`, payload);
  return data;
}

export async function deleteRiskRuleApi(id: number): Promise<Result<void>> {
  const { data } = await client.delete<Result<void>>(`/risk-rules/${id}`);
  return data;
}
