import express from 'express';
import http from 'http';
import { WebSocketServer } from 'ws';
import * as mediasoup from 'mediasoup';
import { v4 as uuidv4 } from 'uuid';
import Room from './rooms/Room.js';

const app = express();
app.use(express.json()); // ✅ Parsear JSON
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

const rooms = new Map<string, Room>();
let worker: mediasoup.types.Worker;
const PORT = 3001;
const mediaCodecs: mediasoup.types.RtpCodecCapability[] = [
    {
        kind: 'audio',
        mimeType: 'audio/opus',
        clockRate: 48000,
        channels: 2,
    },
    {
        kind: 'video',
        mimeType: 'video/VP8',
        clockRate: 90000
    }
];

(async () => {
    worker = await mediasoup.createWorker();
    console.log('Mediasoup worker started');

    // ✅ Crear o recuperar la sala
    function getOrCreateRoom(roomId: string): Room {
        let room = rooms.get(roomId);
        if (!room) {
            room = new Room(worker, mediaCodecs);
            rooms.set(roomId, room);
            console.log(`Sala creada: ${roomId}`);
        }
        return room;
    }

    // --- Endpoints HTTP para media-server ---

    app.post('/get-rtp-capabilities', async (req, res) => {
        const { roomId } = req.body;
        const room = getOrCreateRoom(roomId);
        res.json(room['router'].rtpCapabilities);
    });

    app.post('/create-transport', async (req, res) => {
        const { roomId, senderId } = req.body;
        const room = getOrCreateRoom(roomId);
        const fakeSocket: any = { send: () => {} }; // No usamos socket real
        const peer = await room.handleAction('create-transport', {}, String(senderId), fakeSocket);
        res.json(peer);
    });

    app.post('/connect-transport', async (req, res) => {
        const { roomId, payload } = req.body;
        const room = getOrCreateRoom(roomId);
        const { transportId, dtlsParameters } = payload;
        const fakeSocket: any = { send: () => {} };
        const response = await room.handleAction('connect-transport', { transportId, dtlsParameters }, "dummy", fakeSocket);
        res.json(response);
    });

    app.post('/produce', async (req, res) => {
        const { roomId, payload } = req.body;
        const room = getOrCreateRoom(roomId);
        const { transportId, kind, rtpParameters } = payload;
        const fakeSocket: any = { send: () => {} };
        const response = await room.handleAction('produce', { transportId, kind, rtpParameters }, "dummy", fakeSocket);
        res.json(response);
    });

    app.post('/consume', async (req, res) => {
        const { roomId, payload } = req.body;
        const room = getOrCreateRoom(roomId);
        const { producerPeerId, producerId, transportId, rtpCapabilities } = payload;
        const fakeSocket: any = { send: () => {} };
        const response = await room.handleAction('consume', { producerPeerId, producerId, transportId, rtpCapabilities }, "dummy", fakeSocket);
        res.json(response);
    });

    server.listen(PORT, () => {
        console.log(`🚀 Media server HTTP + WebSocket escuchando en http://localhost:${PORT}`);
    });
})();
