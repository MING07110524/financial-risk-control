export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  total: number;
  records: T[];
}

export interface PageQuery {
  pageNum: number;
  pageSize: number;
}
