export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginUser {
  userId: number;
  username: string;
  realName: string;
  roleCode: string;
  roleName: string;
  token: string;
}

export interface CurrentUser {
  userId: number;
  username: string;
  realName: string;
  roleCode: string;
  roleName: string;
}

export type RoleCode = "ADMIN" | "RISK_USER" | "MANAGER";
