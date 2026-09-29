import { api } from './client.js';
export const signup = data => api.post('/auth/signup', data).then(r => r.data);
export const signin = data => api.post('/auth/signin', data).then(r => r.data);
export const me = () => api.get('/auth/me').then(r => r.data);
