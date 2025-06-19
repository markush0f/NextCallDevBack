const WebSocket = require('ws');

const socket = new WebSocket('ws://localhost:8084/ws/meeting/1/1');

socket.on('open', () => {
  console.log('✅ Conectado');
  socket.send(JSON.stringify({ type: "ping" }));
});

socket.on('message', (msg) => {
  console.log('📩 Mensaje:', msg.toString());
});

socket.on('error', (err) => {
  console.error('❌ Error:', err);
});
