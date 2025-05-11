import Peer from './Peer.js';
export default class Room {
    constructor(worker, mediaCodecs) {
        this.worker = worker;
        this.mediaCodecs = mediaCodecs;
        this.peers = new Map();
        // Inicia la creación del router al instanciar la sala
        this.ready = this.worker.createRouter({ mediaCodecs: this.mediaCodecs })
            .then(router => {
            this.router = router;
            console.log('Router creado');
        })
            .catch(err => {
            console.error('Error creando el router:', err);
            throw err;
        });
    }
    /**
     * Ejecuta una acción sobre la sala, garantizando que el router esté listo
     */
    async handleAction(action, payload, clientId, socket) {
        await this.ready; // Esperar a que router esté inicializado
        if (!this.peers.has(clientId)) {
            this.peers.set(clientId, new Peer(clientId, socket));
        }
        const peer = this.peers.get(clientId);
        switch (action) {
            case 'get-rtp-capabilities':
                return this.getRtpCapabilities();
            case 'create-transport':
                return this.createWebRtcTransport(peer);
            case 'connect-transport':
                return this.connectTransport(peer, payload);
            case 'produce':
                return this.createProducer(peer, payload);
            case 'consume':
                return this.createConsumer(peer, payload);
            default:
                console.warn(`Acción desconocida: ${action}`);
                return { error: 'Acción no reconocida' };
        }
    }
    /** Retorna las capacidades RTP del router */
    getRtpCapabilities() {
        return this.router.rtpCapabilities;
    }
    async createWebRtcTransport(peer) {
        const transport = await this.router.createWebRtcTransport({
            listenIps: [{ ip: '127.0.0.1', announcedIp: undefined }],
            enableUdp: true,
            enableTcp: true,
            preferUdp: true
        });
        peer.addTransport(transport);
        return {
            id: transport.id,
            iceParameters: transport.iceParameters,
            iceCandidates: transport.iceCandidates,
            dtlsParameters: transport.dtlsParameters
        };
    }
    async connectTransport(peer, payload) {
        const transport = peer.getTransport(payload.transportId);
        if (!transport) {
            return { error: 'Transport no encontrado' };
        }
        await transport.connect({ dtlsParameters: payload.dtlsParameters });
        return { connected: true };
    }
    async createProducer(peer, payload) {
        const transport = peer.getTransport(payload.transportId);
        if (!transport) {
            return { error: 'Transport no encontrado' };
        }
        const producer = await transport.produce({
            kind: payload.kind,
            rtpParameters: payload.rtpParameters
        });
        peer.addProducer(producer);
        // Notificar a los demás peers
        for (const [otherId, otherPeer] of this.peers.entries()) {
            if (otherId !== peer.id) {
                otherPeer.socket.send(JSON.stringify({
                    action: 'new-producer',
                    data: {
                        producerId: producer.id,
                        producerPeerId: peer.id,
                        kind: producer.kind
                    }
                }));
            }
        }
        return { id: producer.id };
    }
    async createConsumer(peer, payload) {
        const producerPeer = this.peers.get(payload.producerPeerId);
        if (!producerPeer) {
            return { error: 'Peer del productor no encontrado' };
        }
        const producer = producerPeer.producers.get(payload.producerId);
        if (!producer) {
            return { error: 'Producer no encontrado' };
        }
        const transport = peer.getTransport(payload.transportId);
        if (!transport) {
            return { error: 'Transport no encontrado' };
        }
        if (!this.router.canConsume({
            producerId: producer.id,
            rtpCapabilities: payload.rtpCapabilities
        })) {
            return { error: 'No puedes consumir este stream' };
        }
        const consumer = await transport.consume({
            producerId: producer.id,
            rtpCapabilities: payload.rtpCapabilities,
            paused: false
        });
        peer.addConsumer(consumer);
        return {
            id: consumer.id,
            kind: consumer.kind,
            rtpParameters: consumer.rtpParameters,
            producerId: producer.id
        };
    }
    /** Elimina un peer y cierra sus recursos */
    removePeer(peerId) {
        const peer = this.peers.get(peerId);
        if (!peer)
            return;
        for (const transport of peer.transports.values())
            transport.close();
        for (const producer of peer.producers.values())
            producer.close();
        for (const consumer of peer.consumers.values())
            consumer.close();
        this.peers.delete(peerId);
        console.log(`👤 Peer eliminado de la sala: ${peerId}`);
    }
    /** Obtiene un peer ya existente */
    getPeer(peerId) {
        return this.peers.get(peerId);
    }
}
