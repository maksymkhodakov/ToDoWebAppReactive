export interface Todo {
  id?: number;
  description: string;
  dueDate: string; // ISO date string: YYYY-MM-DD
  checkMark: boolean;
  completionDate?: string;
}

export interface IdDTO {
  ids: number[];
}

export interface LoginData {
  email: string;
  password: string;
}

export interface RegisterData {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface UserDTO {
  id: number;
  email: string;
  userRole: string;
  privileges: { authority: string }[];
}
