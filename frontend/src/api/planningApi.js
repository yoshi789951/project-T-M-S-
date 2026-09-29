import { api } from './client.js';

export const getPlans = () => api.get('/plans').then(r => r.data);
export const createPlan = data => api.post('/plans', data).then(r => r.data);
export const updatePlan = (id, data) => api.put(`/plans/${id}`, data).then(r => r.data);
export const deletePlan = id => api.delete(`/plans/${id}`);

export const getPlanItems = planId => api.get(`/plans/${planId}/items`).then(r => r.data);
export const createPlanItem = (planId, data) => api.post(`/plans/${planId}/items`, data).then(r => r.data);
export const updatePlanItem = (planId, itemId, data) => api.put(`/plans/${planId}/items/${itemId}`, data).then(r => r.data);
export const deletePlanItem = (planId, itemId) => api.delete(`/plans/${planId}/items/${itemId}`);
