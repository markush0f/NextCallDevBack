import express from 'express';
import http from 'http';
import * as mediasoup from 'mediasoup';
import Room from './rooms/Room.js';
import cors from 'cors';

const app = express();
app.use(express.json());
const server = http.createServer(app);

app.use(cors({
    origin: ['http://localhost:5500', 'http://127.0.0.1:5500'],
    credentials: true
}));

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

    async function getOrCreateRoom(roomId: string): Promise<Room> {
        let room = rooms.get(roomId);
        if (!room) {
            room = new Room(worker, mediaCodecs);
            rooms.set(roomId, room);
            console.log(`Sala creada: ${roomId}`);
        }

        await room.ready;
        return room;
    }


    app.post('/get-rtp-capabilities', async (req, res) => {
        const { roomId } = req.body;
        const room = await getOrCreateRoom(roomId);
        res.json(room['router'].rtpCapabilities);
    });

    app.post('/create-transport', async (req, res) => {
        const { roomId, senderId } = req.body;

        const room = await getOrCreateRoom(roomId);
        const fakeSocket: any = { send: () => { } };

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

    // Conectar transport (DTLS handshake)
    app.post('/connect-transport', async (req, res) => {
        const { roomId, senderId, payload } = req.body;
        const { transportId, dtlsParameters } = payload;

        try {
            const room = await getOrCreateRoom(roomId);

            const peer = room.getPeer(String(senderId));
            if (!peer) {
                res.status(404).json({ error: 'Peer no encontrado' });
            }

            const transport = peer!.getTransport(transportId);
            if (!transport) {
                res.status(404).json({ error: 'Transport no encontrado' });
            }

            await transport!.connect({ dtlsParameters });

            res.json({ connected: true });
        } catch (err: any) {
            console.error('Error en connect-transport:', err);
            if (!res.headersSent) {
                res.status(500).json({ error: err.message });
            }
        }
    });



    app.post('/produce', async (req, res) => {
        const { roomId, senderId, payload } = req.body;
        console.log('rtpParameters antes de producir:', payload.rtpParameters);

        console.log('POST /produce body:', JSON.stringify(req.body, null, 2));

        const room = await getOrCreateRoom(roomId);
        const peer = room.getPeer(String(senderId));
        if (!peer) {
            res.status(404).json({ error: 'Peer no encontrado' });
        }

        const transport = peer!.getTransport(payload.transportId);
        if (!transport) {
            res.status(404).json({ error: 'Transport no encontrado' });
        }

        try {
            const producer = await transport!.produce({
                kind: payload.kind,
                rtpParameters: payload.rtpParameters
            });
            peer!.addProducer(producer);
            res.json({ id: producer.id });
        } catch (err: any) {
            console.error('Error en transport.produce:', err);
            res.status(500).json({ error: err.message });
        }
    });

    // Consume media
    app.post('/consume', async (req, res) => {
        const { roomId, senderId, payload } = req.body;
        try {
            const room = await getOrCreateRoom(roomId);
            const { producerPeerId, producerId, transportId, rtpCapabilities } = payload;
            const fakeSocket: any = { send: () => { } };

            // Llamamos a la lógica de consume
            const result = await room.handleAction(
                'consume',
                { producerPeerId, producerId, transportId, rtpCapabilities },
                String(senderId),
                fakeSocket
            );

            // Si handleAction devolvió un error, respondemos 404 y SALIMOS
            if (result && (result as any).error) {
                res.status(404).json({ error: (result as any).error });
            }

            // Si todo ok, devolvemos el resultado y SALIMOS
            res.json(result);

        } catch (err: any) {
            console.error('Error en consume:', err);

            if (!res.headersSent) {
                res.status(500).json({ error: err.message });
            }
        }
    });




    app.get('/health', (req, res) => {
        console.log('→ GET /health');
        res.status(200).send('OK');
    });

    server.listen(PORT, () => {
        console.log(`🚀 Media server HTTP + WebSocket escuchando en http://localhost:${PORT}`);
    });
})
    ();
