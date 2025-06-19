export default class Peer {
    constructor(id, socket) {
        this.id = id;
        this.socket = socket;
        this.transports = new Map();
        this.producers = new Map();
        this.consumers = new Map();
    }
    addTransport(transport) {
        this.transports.set(transport.id, transport);
    }
    getTransport(id) {
        return this.transports.get(id);
    }
    addProducer(producer) {
        this.producers.set(producer.id, producer);
    }
    addConsumer(consumer) {
        this.consumers.set(consumer.id, consumer);
    }
}
