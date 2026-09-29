import { create } from 'zustand';

const readUser = () => {
  try { return JSON.parse(localStorage.getItem('taskflow_user') || 'null'); }
  catch { return null; }
};

export const useAuthStore = create(set => ({
  user: readUser(),
  token: localStorage.getItem('taskflow_token'),
  login: auth => {
    localStorage.setItem('taskflow_token', auth.token);
    localStorage.setItem('taskflow_user', JSON.stringify(auth));
    set({ user: auth, token: auth.token });
  },
  logout: () => {
    localStorage.removeItem('taskflow_token');
    localStorage.removeItem('taskflow_user');
    set({ user: null, token: null });
  }
}));
