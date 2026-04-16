import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

export const STAFF_TOPIC = '/topic/staff';
export const ROOM_TOPIC = '/topic/room/';

export const stompClient = new Client({
    webSocketFactory: () => new SockJS("http://localhost:8080/ws"),

    reconnectDelay: 5000,

    onConnect: () => {
        console.log("Connected to WebSocket");
        stompClient.subscribe(STAFF_TOPIC, (message) => {
            const data = JSON.parse(message.body);
            console.log("Received message on topic", STAFF_TOPIC, ":", data);
        });
    },

    onStompError: (frame) => {
        console.error("Broker error:", frame.headers["message"]);
    },
});