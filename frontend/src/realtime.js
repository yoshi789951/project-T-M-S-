import { io } from 'socket.io-client';
let socket;
export function connectRealtime(token) {
  if (!token) return null;
  if (socket) socket.disconnect();
  const url = import.meta.env.VITE_REALTIME_URL || undefined;
  socket = io(url, { auth: { token }, transports: ['websocket','polling'] });
  return socket;
}
export function disconnectRealtime(){ if(socket){socket.disconnect();socket=null;} }
