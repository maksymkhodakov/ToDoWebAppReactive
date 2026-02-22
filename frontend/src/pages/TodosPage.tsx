import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTodo, deleteTodos, getTodos, updateTodo } from '../api/todos';
import TodoForm from '../components/TodoForm';
import TodoItem from '../components/TodoItem';
import { useAuth } from '../context/AuthContext';
import type { Todo } from '../types';

export default function TodosPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [todos, setTodos] = useState<Todo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');

  const fetchTodos = useCallback(async () => {
    try {
      const data = await getTodos();
      setTodos(data);
    } catch {
      setError('Failed to load todos.');
    }
  }, []);

  useEffect(() => {
    fetchTodos();
  }, [fetchTodos]);

  async function handleCreate(data: Omit<Todo, 'id'>) {
    try {
      const created = await createTodo(data);
      setTodos((prev) => [...prev, created]);
      setShowForm(false);
    } catch {
      setError('Failed to create todo.');
    }
  }

  async function handleUpdate(todo: Todo) {
    try {
      const updated = await updateTodo(todo);
      setTodos((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
    } catch {
      setError('Failed to update todo.');
    }
  }

  async function handleDelete(id: number) {
    try {
      await deleteTodos([id]);
      setTodos((prev) => prev.filter((t) => t.id !== id));
    } catch {
      setError('Failed to delete todo.');
    }
  }

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div style={{ maxWidth: 700, margin: '0 auto', padding: '24px 16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>My Todos</h1>
        <div style={{ display: 'flex', gap: 8 }}>
          <button onClick={() => setShowForm((v) => !v)} style={btnStyle('#2563eb')}>
            {showForm ? 'Cancel' : '+ New Todo'}
          </button>
          <button onClick={handleLogout} style={btnStyle('#6b7280')}>
            Logout
          </button>
        </div>
      </div>

      {error && (
        <p style={{ color: '#dc2626', marginBottom: 12 }}>{error}</p>
      )}

      {showForm && (
        <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 8, padding: 16, marginBottom: 16 }}>
          <h3 style={{ marginBottom: 12 }}>New Todo</h3>
          <TodoForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} submitLabel="Create" />
        </div>
      )}

      {todos.length === 0 ? (
        <p style={{ color: '#6b7280', textAlign: 'center', marginTop: 48 }}>No todos yet. Create one!</p>
      ) : (
        todos.map((todo) => (
          <TodoItem key={todo.id} todo={todo} onUpdate={handleUpdate} onDelete={handleDelete} />
        ))
      )}
    </div>
  );
}

const btnStyle = (bg: string): React.CSSProperties => ({
  padding: '8px 16px',
  background: bg,
  color: '#fff',
  border: 'none',
  borderRadius: 6,
  cursor: 'pointer',
  fontSize: 14,
  fontWeight: 600,
});
