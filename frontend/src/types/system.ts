export interface RoleVO {
  id: number;
  roleName: string;
  roleCode: string;
  remark?: string;
}

export interface UserVO {
  id: number;
  username: string;
  realName: string;
  phone?: string;
  status: number;
  roleName: string;
  roleCode: string;
  roleIds?: number[];
  createTime?: string;
  updateTime?: string;
}

export interface LogVO {
  id: number;
  moduleName: string;
  operationType: string;
  operationDesc: string;
  operator?: string;
  operatorId?: number;
  operationTime: string;
}
