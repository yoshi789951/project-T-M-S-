import { api } from './client.js';
export const getUsers = () => api.get('/admin/users').then(r => r.data);
export const setRole = (id,role) => api.patch(`/admin/users/${id}/role`, null, { params: { role } }).then(r => r.data);
export const setActive = (id,active) => api.patch(`/admin/users/${id}/active`, null, { params: { active } }).then(r => r.data);
