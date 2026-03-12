import type { CurrentUser, RoleCode } from "@/types/auth";
import type {
  AssessmentDetailVO,
  RiskDataDetailVO,
  RiskIndexVO,
  RiskRuleVO,
  RoleOption,
  UserSummary,
  WarningDetailVO,
  WarningHandleRecordVO,
} from "@/types/risk";

export interface MockUser extends UserSummary {
  roleId: number;
}

export interface MockAssessment extends AssessmentDetailVO {
  assessmentBy: number;
}

export interface MockWarning extends WarningDetailVO {
  handleRecords: WarningHandleRecordVO[];
}

export interface MockDb {
  roles: RoleOption[];
  users: MockUser[];
  riskIndexes: RiskIndexVO[];
  riskRules: RiskRuleVO[];
  riskData: RiskDataDetailVO[];
  assessments: MockAssessment[];
  warnings: MockWarning[];
  nextIds: Record<string, number>;
}

export interface DemoAccount {
  username: string;
  realName: string;
  roleCode: RoleCode;
  roleName: string;
}

export interface MockSession {
  token: string;
  user: CurrentUser;
}
