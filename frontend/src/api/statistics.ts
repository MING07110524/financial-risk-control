import client from "@/api/client";
import { compactParams } from "@/api/params";
import type { Result } from "@/types/common";
import type {
  DashboardStatisticsVO,
  HandleSummaryStatisticsVO,
  RiskLevelStatisticsVO,
  StatisticsQuery,
  WarningTrendStatisticsVO,
  WarningVO,
} from "@/types/risk";

export async function getDashboardStatisticsApi(): Promise<Result<DashboardStatisticsVO>> {
  const { data } = await client.get<Result<DashboardStatisticsVO>>("/statistics/dashboard");
  return data;
}

export async function listRecentWarningsApi(limit = 5): Promise<Result<WarningVO[]>> {
  const { data } = await client.get<Result<WarningVO[]>>("/statistics/recent-warnings", {
    params: { limit },
  });
  return data;
}

export async function getRiskLevelStatisticsApi(query: StatisticsQuery): Promise<Result<RiskLevelStatisticsVO[]>> {
  const { data } = await client.get<Result<RiskLevelStatisticsVO[]>>("/statistics/risk-level", {
    params: compactParams(query),
  });
  return data;
}

export async function getWarningTrendStatisticsApi(query: StatisticsQuery): Promise<Result<WarningTrendStatisticsVO[]>> {
  const { data } = await client.get<Result<WarningTrendStatisticsVO[]>>("/statistics/warning-trend", {
    params: compactParams(query),
  });
  return data;
}

export async function getHandleSummaryStatisticsApi(query: StatisticsQuery): Promise<Result<HandleSummaryStatisticsVO[]>> {
  const { data } = await client.get<Result<HandleSummaryStatisticsVO[]>>("/statistics/handle-summary", {
    params: compactParams(query),
  });
  return data;
}
