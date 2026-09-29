import http from 'node:http';
import { Server } from 'socket.io';
import Redis from 'ioredis';
import jwt from 'jsonwebtoken';

const port = Number(process.env.PORT || 4000);
const jwtSecret = process.env.JWT_SECRET || 'taskflow-dev-secret-key-change-me-1234567890';
const redisUrl = process.env.REDIS_URL || 'redis://localhost:6379';
const channel = process.env.REALTIME_CHANNEL || 'taskflow-events';
const allowedOrigins = (process.env.CORS_ALLOWED_ORIGINS || 'http://localhost:5173,http://localhost:3000,http://localhost')
  .split(',').map(v => v.trim()).filter(Boolean);

const server = http.createServer((req, res) => {
  if (req.url === '/health') {
    res.writeHead(200, { 'content-type': 'application/json' });
    res.end(JSON.stringify({ status: 'UP' }));
    return;
  }
  res.writeHead(404);
  res.end();
});

const io = new Server(server, {
  cors: { origin: allowedOrigins, credentials: true },
  transports: ['websocket', 'polling']
});

io.use((socket, next) => {
  const token = socket.handshake.auth?.token;
  if (!token) return next(new Error('Authentication required'));
  try {
    socket.user = jwt.verify(token, jwtSecret, { algorithms: ['HS256', 'HS384', 'HS512'] });
    return next();
  } catch {
    return next(new Error('Invalid or expired token'));
  }
});

io.on('connection', socket => {
  socket.emit('task-event', { type: 'REALTIME_CONNECTED', timestamp: new Date().toISOString() });
});

const subscriber = new Redis(redisUrl, { lazyConnect: false, maxRetriesPerRequest: null });
subscriber.on('error', err => console.error('[redis]', err.message));
subscriber.subscribe(channel).then(() => console.log(`Subscribed to Redis channel: ${channel}`));
subscriber.on('message', (_channel, message) => {
  try {
    io.emit('task-event', JSON.parse(message));
  } catch {
    io.emit('task-event', { type: 'TASKFLOW_EVENT', message, timestamp: new Date().toISOString() });
  }
});

server.listen(port, () => console.log(`TaskFlow realtime service listening on http://localhost:${port}`));
