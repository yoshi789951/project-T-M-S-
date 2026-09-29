import { create } from 'zustand';
const initial = localStorage.getItem('taskflow_theme') || 'dark';
export const useUiStore = create(set => ({
  theme: initial,
  toggleTheme: () => set(state => { const next = state.theme === 'dark' ? 'light' : 'dark'; localStorage.setItem('taskflow_theme', next); return { theme: next }; })
}));
