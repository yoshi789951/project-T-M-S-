import { api } from './client.js';

export const getDashboard = () => api.get('/dashboard').then(r => r.data);
export const getTasks = params => api.get('/tasks', { params }).then(r => r.data);
export const getMyTasks = () => api.get('/tasks/mine').then(r => r.data);
export const createTask = data => api.post('/tasks', data).then(r => r.data);
export const updateTask = (id, data) => api.put(`/tasks/${id}`, data).then(r => r.data);
export const updateTaskStatus = (id, status) => api.patch(`/tasks/${id}/status`, { status }).then(r => r.data);
export const deleteTask = id => api.delete(`/tasks/${id}`);
export const getComments = id => api.get(`/tasks/${id}/comments`).then(r => r.data);
export const addComment = (id, body) => api.post(`/tasks/${id}/comments`, { body }).then(r => r.data);
export const getTaskActivity = id => api.get(`/tasks/${id}/activity`).then(r => r.data);
