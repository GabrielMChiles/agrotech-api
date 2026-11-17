export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ProblemDetail {
  title?: string;
  status?: number;
  detail?: string;
  erros?: string[];
}

