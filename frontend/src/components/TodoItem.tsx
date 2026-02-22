import { useState } from 'react';
import type { Todo } from '../types';
import TodoForm from './TodoForm';

interface Props {
  todo: Todo;
  onUpdate: (todo: Todo) => void;
  onDelete: (id: number) => void;
}

export default function TodoItem({ todo, onUpdate, onDelete }: Props) {
  const [editing, setEditing] = useState(false);

  if (editing) {
    return (
      <div style={cardStyle}>
        <TodoForm
          initial={todo}
          onSubmit={(data) => {
            onUpdate({ ...data, id: todo.id });
            setEditing(false);
          }}
          onCancel={() => setEditing(false)}
          submitLabel="Update"
        />
      </div>
    );
  }

  return (
    <div style={{ ...cardStyle, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <div>
        <div style={{ fontWeight: 600, fontSize: 15, textDecoration: todo.checkMark ? 'line-through' : 'none' }}>
          {todo.description}
        </div>
        <div style={{ fontSize: 12, color: '#6b7280', marginTop: 2 }}>
          Due: {todo.dueDate}
          {todo.completionDate && ` · Completed: ${todo.completionDate}`}
        </div>
      </div>
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={() => setEditing(true)} style={btnStyle('#2563eb')}>Edit</button>
        <button onClick={() => onDelete(todo.id!)} style={btnStyle('#dc2626')}>Delete</button>
      </div>
    </div>
  );
}

const cardStyle: React.CSSProperties = {
  background: '#fff',
  border: '1px solid #e5e7eb',
  borderRadius: 8,
  padding: '12px 16px',
  marginBottom: 10,
};

const btnStyle = (bg: string): React.CSSProperties => ({
  padding: '6px 12px',
  background: bg,
  color: '#fff',
  border: 'none',
  borderRadius: 6,
  cursor: 'pointer',
  fontSize: 13,
});
