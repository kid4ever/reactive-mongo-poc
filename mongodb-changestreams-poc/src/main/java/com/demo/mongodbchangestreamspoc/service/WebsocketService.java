package com.demo.mongodbchangestreamspoc.service;

import com.demo.mongodbchangestreamspoc.domain.ProductEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebsocketService {

    private static final String TOPIC = "/topic/wsevents";

    private SimpMessagingTemplate template;

    public WebsocketService(SimpMessagingTemplate template) {
        this.template = template;
    }

    /**
     * Publish Product events to the defined topic for anyone to be able to listen to.
     * @param event
     */
    public void publishEvent(ProductEvent event) {
        template.convertAndSend(TOPIC, event);
    }
}
