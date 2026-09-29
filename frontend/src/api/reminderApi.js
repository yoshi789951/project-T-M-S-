import { api } from './client.js';
export const getReminders = () => api.get('/reminders').then(r => r.data);
export const getUpcomingReminders = () => api.get('/reminders/upcoming').then(r => r.data);
export const createReminder = data => api.post('/reminders', data).then(r => r.data);
export const updateReminder = (id,data) => api.put(`/reminders/${id}`, data).then(r => r.data);
export const updateReminderStatus = (id,status,read) => api.patch(`/reminders/${id}/status`, { status, read }).then(r => r.data);
export const deleteReminder = id => api.delete(`/reminders/${id}`);
