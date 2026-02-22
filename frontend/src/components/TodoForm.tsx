import { useState } from 'react';
import type { Todo } from '../types';

interface Props {
  initial?: Partial<Todo>;
  onSubmit: (todo: Omit<Todo, 'id'>) => void;
  onCancel?: () => void;
  submitLabel?: string;
}

export default function TodoForm({ initial, onSubmit, onCancel, submitLabel = 'Save' }: Props) {
  const [description, setDescription] = useState(initial?.description ?? '');
  const [dueDate, setDueDate] = useState(initial?.dueDate ?? '');
  const [checkMark, setCheckMark] = useState(initial?.checkMark ?? false);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit({ description, dueDate, checkMark });
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <input
        required
        placeholder="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        style={inputStyle}
      />
      <input
        required
        type="date"
        value={dueDate}
        onChange={(e) => setDueDate(e.target.value)}
        style={inputStyle}
      />
      <label style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <input
          type="checkbox"
          checked={checkMark}
          onChange={(e) => setCheckMark(e.target.checked)}
        />
        Completed
      </label>
      <div style={{ display: 'flex', gap: 8 }}>
        <button type="submit" style={btnStyle('#2563eb')}>
          {submitLabel}
        </button>
        {onCancel && (
          <button type="button" onClick={onCancel} style={btnStyle('#6b7280')}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}

const inputStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderRadius: 6,
  border: '1px solid #d1d5db',
  fontSize: 14,
};

const btnStyle = (bg: string): React.CSSProperties => ({
  padding: '8px 16px',
  background: bg,
  color: '#fff',
  border: 'none',
  borderRadius: 6,
  cursor: 'pointer',
  fontSize: 14,
});
