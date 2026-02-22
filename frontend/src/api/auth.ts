import client from './client';
import type { LoginData, LoginResponse, RegisterData, UserDTO } from '../types';

export const login = (data: LoginData) =>
  client.post<LoginResponse>('/login', data).then((r) => r.data);

export const register = (data: RegisterData) =>
  client.post<void>('/register', data);

export const me = () =>
  client.get<UserDTO>('/me').then((r) => r.data);
