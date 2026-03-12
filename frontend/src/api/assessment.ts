import client from "@/api/client";
import { compactParams } from "@/api/params";
import type { PageResult, Result } from "@/types/common";
import type { AssessmentDetailVO, AssessmentQuery, AssessmentVO } from "@/types/risk";

export async function pageAssessmentsApi(query: AssessmentQuery): Promise<Result<PageResult<AssessmentVO>>> {
  const { data } = await client.get<Result<PageResult<AssessmentVO>>>("/assessments", {
    params: compactParams(query),
  });
  return data;
}

export async function getAssessmentDetailApi(id: number): Promise<Result<AssessmentDetailVO>> {
  const { data } = await client.get<Result<AssessmentDetailVO>>(`/assessments/${id}`);
  return data;
}

export async function executeAssessmentApi(riskDataId: number): Promise<Result<AssessmentDetailVO>> {
  const { data } = await client.post<Result<AssessmentDetailVO>>(`/assessments/${riskDataId}/execute`);
  return data;
}
