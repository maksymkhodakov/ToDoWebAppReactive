import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await login({ email, password });
      navigate('/');
    } catch {
      setError('Invalid credentials. Please try again.');
    }
  }

  return (
    <div style={pageStyle}>
      <div style={cardStyle}>
        <h2 style={{ marginBottom: 24 }}>Sign In</h2>
        {error && <p style={{ color: '#dc2626', marginBottom: 12 }}>{error}</p>}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <input
            required
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={inputStyle}
          />
          <input
            required
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={inputStyle}
          />
          <button type="submit" style={btnStyle}>Sign In</button>
        </form>
        <p style={{ marginTop: 16, fontSize: 14 }}>
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  );
}

const pageStyle: React.CSSProperties = {
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: '#f9fafb',
};

const cardStyle: React.CSSProperties = {
  background: '#fff',
  padding: 32,
  borderRadius: 12,
  boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
  width: 360,
};

const inputStyle: React.CSSProperties = {
  padding: '10px 12px',
  borderRadius: 6,
  border: '1px solid #d1d5db',
  fontSize: 14,
};

const btnStyle: React.CSSProperties = {
  padding: '10px',
  background: '#2563eb',
  color: '#fff',
  border: 'none',
  borderRadius: 6,
  cursor: 'pointer',
  fontSize: 14,
  fontWeight: 600,
};
