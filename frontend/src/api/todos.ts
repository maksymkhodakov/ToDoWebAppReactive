import client from './client';
import type { IdDTO, Todo } from '../types';

export const getTodos = () =>
  client.get<Todo[]>('/todos').then((r) => r.data);

export const createTodo = (todo: Omit<Todo, 'id'>) =>
  client.post<Todo>('/todo/create', todo).then((r) => r.data);

export const updateTodo = (todo: Todo) =>
  client.put<Todo>('/todo/update', todo).then((r) => r.data);

export const deleteTodos = (ids: number[]) =>
  client.delete<Todo[]>('/todo/delete', { data: { ids } as IdDTO }).then((r) => r.data);
