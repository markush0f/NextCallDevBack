import { WebSocket } from 'ws';
import {
  WebRtcTransport,
  Producer,
  Consumer
} from 'mediasoup/node/lib/types';

export default class Peer {
  id: string;
  socket: WebSocket;
  transports: Map<string, WebRtcTransport>;
  producers: Map<string, Producer>;
  consumers: Map<string, Consumer>;

  constructor(id: string, socket: WebSocket) {
    this.id = id;
    this.socket = socket;
    this.transports = new Map();
    this.producers = new Map();
    this.consumers = new Map();
  }

  addTransport(transport: WebRtcTransport) {
    this.transports.set(transport.id, transport);
  }

  getTransport(id: string): WebRtcTransport | undefined {
    return this.transports.get(id);
  }

  addProducer(producer: Producer) {
    this.producers.set(producer.id, producer);
  }

  addConsumer(consumer: Consumer) {
    this.consumers.set(consumer.id, consumer);
  }
}
