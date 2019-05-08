## POC for publishing MongoDB Chagestream events to third parties

### Technologies
* Java version: 12.0.1
* Spring Version 5.1.4
* Spring Boot version: 2.1.4
* MongoDb version: 4.0.9
* Node version: 12.1.0

### mongodb-changestreams-poc

Spring Boot Application connects to a mongodb server(local) and listens to change stream events.

It exposes a Streaming Endpoint(localhost:8080/products/events) that responds with some event details every time a change happens in the DB.

It also continously publishes the same details to a Websokect topic (localhost:8080/ws/topic/wsevents).

#### Steps to run:
```
mvn clean install -DskipTests
cd ./target
java -jar mongodb-changestreams-poc.jar
```
Or alternatively import and run from IDE.

### ws-client
A simple Websocket STOMP client to connect to the service and subscribe to the wsevents topic.

#### Steps to run:
After the service is started:
```
npm install
node client.js
```

