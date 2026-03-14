import type { PageQuery } from "@/types/common";
import type { RoleCode } from "@/types/auth";

export type UserStatus = 0 | 1;
export type RiskDataStatus = 0 | 1 | 2 | 3;
export type AssessmentStatus = 0 | 1;
export type WarningStatus = 0 | 1 | 2;
export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export interface RoleOption {
  id: number;
  roleCode: RoleCode;
  roleName: string;
  remark: string;
}

export interface UserSummary {
  id: number;
  username: string;
  realName: string;
  roleCode: RoleCode;
  roleName: string;
  phone: string;
  status: UserStatus;
}

export interface RiskIndexQuery {
  status?: UserStatus;
  indexName?: string;
}

export interface RiskIndexVO {
  id: number;
  indexName: string;
  indexCode: string;
  weightValue: number;
  indexDesc: string;
  status: UserStatus;
}

export interface RiskIndexCreateDTO {
  indexName: string;
  indexCode: string;
  weightValue: number | null;
  indexDesc: string;
  status: UserStatus;
}

export interface RiskIndexUpdateDTO {
  indexName: string;
  indexCode: string;
  weightValue: number | null;
  indexDesc: string;
}

export interface RiskIndexStatusDTO {
  status: UserStatus;
}

export interface RiskRuleVO {
  id: number;
  indexId: number;
  indexName: string;
  scoreMin: number;
  scoreMax: number;
  scoreValue: number;
  warningLevel: RiskLevel;
}

export interface RiskRuleCreateDTO {
  indexId: number;
  scoreMin: number | null;
  scoreMax: number | null;
  scoreValue: number | null;
  warningLevel: RiskLevel;
}

export interface RiskRuleUpdateDTO {
  scoreMin: number | null;
  scoreMax: number | null;
  scoreValue: number | null;
  warningLevel: RiskLevel;
}

export interface RiskDataIndexValueItemDTO {
  indexId: number;
  indexValue: number | null;
}

export interface RiskDataCreateDTO {
  businessNo: string;
  customerName: string;
  businessType: string;
  riskDesc: string;
  indexValues: RiskDataIndexValueItemDTO[];
}

export interface RiskDataQuery extends PageQuery {
  businessNo: string;
  customerName: string;
  businessType: string;
  dataStatus: RiskDataStatus | "";
}

export interface RiskDataVO {
  id: number;
  businessNo: string;
  customerName: string;
  businessType: string;
  riskDesc: string;
  dataStatus: RiskDataStatus;
  createBy: number;
  createByName: string;
  createTime: string;
  updateTime: string;
}

export interface RiskDataIndexValueVO {
  indexId: number;
  indexCode: string;
  indexName: string;
  indexValue: number;
  weightValue: number;
}

export interface RiskDataDetailVO extends RiskDataVO {
  indexValues: RiskDataIndexValueVO[];
  missingEnabledIndexNames?: string[];
}

export interface AssessmentQuery extends PageQuery {
  businessNo: string;
  riskLevel: RiskLevel | "";
  assessmentStatus: AssessmentStatus | "";
  startTime: string;
  endTime: string;
  riskDataId?: number | null;
}

export interface AssessmentVO {
  id: number;
  riskDataId: number;
  businessNo: string;
  customerName: string;
  totalScore: number;
  riskLevel: RiskLevel;
  assessmentStatus: AssessmentStatus;
  assessmentTime: string;
  assessmentByName: string;
  warningGenerated: boolean;
}

export interface AssessmentIndexResultVO {
  indexId: number;
  indexCode: string;
  indexName: string;
  indexValue: number;
  weightValue: number;
  scoreValue: number;
  weightedScore: number;
  warningLevel: RiskLevel;
}

export interface WarningSimpleVO {
  warningId: number;
  warningCode: string;
  warningLevel: RiskLevel;
  warningStatus: WarningStatus;
  warningContent: string;
}

export interface AssessmentDetailVO extends AssessmentVO {
  businessType: string;
  riskDesc: string;
  dataStatus: RiskDataStatus;
  indexResults: AssessmentIndexResultVO[];
  warningInfo: WarningSimpleVO | null;
}

export interface WarningQuery extends PageQuery {
  warningCode: string;
  warningLevel: RiskLevel | "";
  warningStatus: WarningStatus | "";
  startTime: string;
  endTime: string;
}

export interface WarningVO {
  id: number;
  assessmentId: number;
  riskDataId: number;
  warningCode: string;
  warningLevel: RiskLevel;
  warningContent: string;
  businessNo: string;
  customerName: string;
  warningStatus: WarningStatus;
  createTime: string;
}

export interface WarningHandleRecordVO {
  id: number;
  warningId: number;
  handleUserId: number;
  handleUserName: string;
  handleOpinion: string;
  handleResult: string;
  nextStatus: WarningStatus;
  handleTime: string;
}

export interface WarningDetailVO extends WarningVO {
  businessType: string;
  riskDesc: string;
  totalScore: number;
  riskLevel: RiskLevel;
}

export interface WarningHandleDTO {
  handleOpinion: string;
  handleResult: string;
  nextStatus: 1 | 2;
}

export interface StatisticsQuery {
  startTime: string;
  endTime: string;
  riskLevel: RiskLevel | "";
  warningStatus: WarningStatus | "";
}

export interface DashboardStatisticsVO {
  riskDataCount: number;
  assessmentCount: number;
  warningCount: number;
  handledWarningCount: number;
  highRiskCount: number;
}

export interface RiskLevelStatisticsVO {
  riskLevel: RiskLevel;
  count: number;
}

export interface WarningTrendStatisticsVO {
  date: string;
  total: number;
  pending: number;
  handled: number;
}

export interface HandleSummaryStatisticsVO {
  warningStatus: WarningStatus;
  label: string;
  count: number;
}
