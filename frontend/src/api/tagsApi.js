import { api } from './client.js';

export const getTags = () => api.get('/tags').then(r => r.data);
