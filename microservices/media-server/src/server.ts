import express from 'express';
import http from 'http';
import { WebSocketServer } from 'ws';
import * as mediasoup from 'mediasoup';
import Room from './rooms/Room.js';

const app = express();
app.use(express.json());
const server = http.createServer(app);

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


    app.post('/get-rtp-capabilities', async (req, res) => {
        const { roomId } = req.body;
        const room = getOrCreateRoom(roomId);
        res.json(room['router'].rtpCapabilities);
    });

    app.post('/create-transport', async (req, res) => {
        const { roomId, senderId } = req.body;
    
        const room = getOrCreateRoom(roomId);
        const fakeSocket: any = { send: () => {} }; 
    
        const peerId = String(senderId);
    
        if (!room['peers'].has(peerId)) {
            room['peers'].set(peerId, new (await import('./rooms/Peer.js')).default(peerId, fakeSocket));
        }
    
        const peer = room['peers'].get(peerId);
    
        const transportOptions = await room['router'].createWebRtcTransport({
            listenIps: [{ ip: '127.0.0.1', announcedIp: undefined }],
            enableUdp: true,
            enableTcp: true,
            preferUdp: true
        });
    
        peer!.addTransport(transportOptions);
    
        res.json({
            id: transportOptions.id,
            iceParameters: transportOptions.iceParameters,
            iceCandidates: transportOptions.iceCandidates,
            dtlsParameters: transportOptions.dtlsParameters
        });
    });
    

    app.post('/connect-transport', async (req, res) => {
        const { roomId, payload } = req.body;
        const { transportId, dtlsParameters } = payload;
    
        const room = getOrCreateRoom(roomId);
        const peer = room['peers'].values().next().value; 
    
        const transport = peer!.transports.get(transportId);
        if (!transport) {
            res.status(404).json({ error: 'Transport no encontrado' });
        }
    
        await transport!.connect({ dtlsParameters });
    
        res.json({ connected: true });
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
