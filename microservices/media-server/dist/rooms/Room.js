import Peer from './Peer.js';
export default class Room {
    constructor(worker, mediaCodecs) {
        this.worker = worker;
        this.mediaCodecs = mediaCodecs;
        this.peers = new Map();
        this.createRouter();
    }
    async createRouter() {
        this.router = await this.worker.createRouter({ mediaCodecs: this.mediaCodecs });
        console.log('Router creado');
    }
    async handleAction(action, payload, clientId, socket) {
        if (!this.peers.has(clientId)) {
            this.peers.set(clientId, new Peer(clientId, socket));
        }
        const peer = this.peers.get(clientId);
        switch (action) {
            // Devuelve las capacidades RTP del router de mediasoup, los tipos de audio y video que soporta.
            // El cliente necesita esta info antes de negociar con WebRTC. Lo usa para crear su RTCPeerConnection y rtpParameters.
            case 'get-rtp-capabilities':
                return this.router.rtpCapabilities;
            // Crea un nuevo WebRTC transport, que es el canal WebRTC entre el cliente y el servidor.
            // Para enviar o recibir media.
            case 'create-transport':
                return await this.createWebRtcTransport(peer);
            // Conecta el WebRTC transport usando los DTLS parameters que el cliente.
            // Finaliza la negociacion segura con WebRTC.
            case 'connect-transport':
                return await this.connectTransport(peer, payload);
            // Crea un producer, el usuario envía media al servidor.
            // Es lo que representa el stream saliento de un usuario. Otros usuarios pueden consumirlo.
            case 'produce':
                return await this.createProducer(peer, payload);
            // Crea un consumer, es como un canal por donde el usuario actual va a recibir media desde otro usuario.
            // El servidor crea esta conexion para que el cliente reciba el stream de otro usuario.
            case 'consume':
                return await this.createConsumer(peer, payload);
            default:
                console.warn(`Acción desconocida: ${action}`);
                return { error: 'Acción no reconocida' };
        }
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
        if (!transport)
            return { error: 'Transport no encontrado' };
        await transport.connect({ dtlsParameters: payload.dtlsParameters });
        return { connected: true };
    }
    async createProducer(peer, payload) {
        const transport = peer.getTransport(payload.transportId);
        if (!transport)
            return { error: 'Transport no encontrado' };
        const producer = await transport.produce({
            kind: payload.kind,
            rtpParameters: payload.rtpParameters
        });
        peer.addProducer(producer);
        // Notificamos a los demas peers que hay un nuevo producer.
        for (const [peerId, peer] of this.peers.entries()) {
            if (peerId !== peer.id) {
                peer.socket.send(JSON.stringify({
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
        if (!producerPeer)
            return { error: 'Peer del productor no encontrado' };
        const producer = producerPeer.producers.get(payload.producerId);
        if (!producer)
            return { error: 'Producer no encontrado' };
        const transport = peer.getTransport(payload.transportId);
        if (!transport)
            return { error: 'Transport no encontrado' };
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
    removePeer(id) {
        const peer = this.peers.get(id);
        if (!peer)
            return;
        for (const transport of peer.transports.values()) {
            transport.close();
        }
        for (const producer of peer.producers.values()) {
            producer.close();
        }
        for (const consumer of peer.consumers.values()) {
            consumer.close();
        }
        this.peers.delete(id);
        console.log(`👤 Peer eliminado de la sala: ${id}`);
    }
}
