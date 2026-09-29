import axios from 'axios';
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api' });
api.interceptors.request.use(config => {
  const token = localStorage.getItem('taskflow_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(r => r, error => {
  if (error.response?.status === 401) {
    localStorage.removeItem('taskflow_token');
    localStorage.removeItem('taskflow_user');
    window.dispatchEvent(new Event('taskflow:unauthorized'));
  }
  return Promise.reject(error);
});
export const messageFrom = error => error.response?.data?.message || error.response?.data || error.message || 'Request failed';
