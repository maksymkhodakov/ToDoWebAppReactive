import axios from 'axios';

const TOKEN_KEY = 'todo_jwt';

const client = axios.create({
  baseURL: '/api',
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export { TOKEN_KEY };
export default client;
