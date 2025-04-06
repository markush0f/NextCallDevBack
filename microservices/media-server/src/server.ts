import { createServer } from 'http';
import { WebSocketServer } from 'ws';
import * as mediasoup from 'mediasoup';
import { v4 as uuidv4 } from 'uuid';
import Room from './rooms/Room';

const rooms = new Map<string, Room>();
let worker: mediasoup.types.Worker;
const PORT = 3001;
const mediaCodecs: mediasoup.types.RtpCodecCapability[] = [
    {
        kind: 'audio',
        mimeType: 'audio/opus',
        clockRate: 48000,
        channels: 2
    },
    {
        kind: 'video',
        mimeType: 'video/VP8',
        clockRate: 90000
    }
];

(async () => {
    // El worker es el proceso que maneja todo lo que tiene que ver con transmisión de media.
    worker = await mediasoup.createWorker();

    console.log('Mediasoup worker started');

    // Crea un servidor WebSocket
    const httpServer = createServer();
    const wss = new WebSocketServer({ server: httpServer });

    wss.on('connection', (ws) => {
        const clientId = uuidv4();
        console.log(`Cliente conectado: ${clientId}`);

        ws.on('message', async (message) => {
            const { action, roomId, payload } = JSON.parse(message.toString());

            let room = rooms.get(roomId);
            if (!room) {
                room = new Room(worker, mediaCodecs);
                rooms.set(roomId, room);
                console.log(`Sala creada: ${roomId}`);
            }

            const response = await room.handleAction(action, payload, clientId, ws);
            if (response) {
                ws.send(JSON.stringify({ action, data: response }));
            }
        });

        ws.on('close', () => {
            for (const room of rooms.values()) {
                room.removePeer(clientId);
            }
            console.log(`🔌 Cliente desconectado: ${clientId}`);
        });
    });


    httpServer.listen(PORT, () => {
        console.log(`Media server WebSocket escuchando en ws://localhost:3001 ${PORT}`);
    });
})();
