import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import {eventBus} from "@/services/event-bus.ts";

export const STAFF_TOPIC = '/topic/staff';
export const INVENTORY_TOPIC = '/topic/inventory';
export const ROOM_TOPIC = '/topic/room/';

export const stompClient = new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),

    reconnectDelay: 5000,

    onConnect: () => {
        console.log("STOMP connected");
        const sub = stompClient.subscribe(STAFF_TOPIC, (message) => {
            console.log("FULL MESSAGE OBJECT:", message);
            console.log("HEADERS:", message.headers);
            console.log("BODY:", message.body);

            const data = JSON.parse(message.body);
            eventBus.emit("new-request", data);
            console.log("Received message on topic", STAFF_TOPIC, ":", data);
        });

        const sub2 = stompClient.subscribe(INVENTORY_TOPIC, (message) => {
            const data = JSON.parse(message.body);
            eventBus.emit("new-inventory-item", data);
            console.log("Received message on topic", INVENTORY_TOPIC, ":", data);
        });

        console.log("Subscribed:", sub.id);
    },

    onStompError: (frame) => {
        console.error("Broker error:", frame.headers["message"]);
    },
});