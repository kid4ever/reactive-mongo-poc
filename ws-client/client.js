#!/usr/bin/env node
const Websocket = require('ws');
const Stomp = require('@stomp/stompjs');
const url = "http://localhost:8080/ws";

const socket = new Websocket(url);
const client = Stomp.Stomp.over(socket);

client.onConnect = function() {
    client.debug("Connected");
    client.subscribe("/topic/wsevents", function (message) {
        if(message.body) {
            client.debug("Received: " + message.body);
        }
    })
};

client.onStompError = function (frame) {
    console.log('Broker reported error: ' + frame.headers['message']);
    console.log('Additional details: ' + frame.body);
};

client.onWebSocketError = function(event) {
    console.log('Websocket reported error: ' + event.returnValue);
    console.log('Additional details: ' + event);
};

client.activate();
