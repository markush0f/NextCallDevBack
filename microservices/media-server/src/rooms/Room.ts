import * as mediasoup from 'mediasoup';
import { WebSocket } from 'ws';
import Peer from './Peer.js';

export default class Room {
  private readonly peers: Map<string, Peer> = new Map();
  private router!: mediasoup.types.Router;
  public readonly ready: Promise<void>;

  constructor(
    private readonly worker: mediasoup.types.Worker,
    private readonly mediaCodecs: mediasoup.types.RtpCodecCapability[]
  ) {

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
  public async handleAction(
    action: string,
    payload: any,
    clientId: string,
    socket: WebSocket
  ) {
    await this.ready;

    if (!this.peers.has(clientId)) {
      this.peers.set(clientId, new Peer(clientId, socket));
    }
    const peer = this.peers.get(clientId)!;

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
  public getRtpCapabilities(): mediasoup.types.RtpCapabilities {
    return this.router.rtpCapabilities;
  }

  private async createWebRtcTransport(peer: Peer) {
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

  private async connectTransport(peer: Peer, payload: any) {
    const transport = peer.getTransport(payload.transportId);
    if (!transport) {
      return { error: 'Transport no encontrado' };
    }
    await transport.connect({ dtlsParameters: payload.dtlsParameters });
    return { connected: true };
  }

  private async createProducer(peer: Peer, payload: any) {
    const transport = peer.getTransport(payload.transportId);
    if (!transport) {
      return { error: 'Transport no encontrado' };
    }

    const producer = await transport.produce({
      kind: payload.kind,
      rtpParameters: payload.rtpParameters
    });

    peer.addProducer(producer);

    for (const [otherId, otherPeer] of this.peers.entries()) {
      if (otherId !== peer.id) {
        otherPeer.socket.send(
          JSON.stringify({
            action: 'new-producer',
            data: {
              producerId: producer.id,
              producerPeerId: peer.id,
              kind: producer.kind
            }
          })
        );
      }
    }
    return { id: producer.id };
  }

  private async createConsumer(peer: Peer, payload: any) {
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

  public removePeer(peerId: string) {
    const peer = this.peers.get(peerId);
    if (!peer) return;
    for (const transport of peer.transports.values()) transport.close();
    for (const producer of peer.producers.values()) producer.close();
    for (const consumer of peer.consumers.values()) consumer.close();
    this.peers.delete(peerId);
    console.log(`👤 Peer eliminado de la sala: ${peerId}`);
  }

  public getPeer(peerId: string): Peer | undefined {
    return this.peers.get(peerId);
  }
}
