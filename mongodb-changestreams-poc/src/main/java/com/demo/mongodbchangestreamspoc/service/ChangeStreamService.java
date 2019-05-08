package com.demo.mongodbchangestreamspoc.service;

import com.demo.mongodbchangestreamspoc.domain.Product;
import com.demo.mongodbchangestreamspoc.domain.ProductEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

@Service
public class ChangeStreamService {

    private static final String COLLECTION = "product";

    private ReactiveMongoTemplate mongoTemplate;
    private Flux<ProductEvent> changeStreamFlux;

    public ChangeStreamService(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Method that processes Change Stream events on the Collection and transforms them into Product events.
     * A class attribute is used because each Change Stream will open a new connection to MongoDb
     * which can lead to performance issues.
     * Aggregation can be added to filter events from Mongo
     * @return A stream of Product Events
     */
    public Flux<ProductEvent> productChangeEvents() {
        if(changeStreamFlux != null) {
            return changeStreamFlux;
        } else {
            changeStreamFlux = mongoTemplate.changeStream(COLLECTION, ChangeStreamOptions.empty(), Product.class)
                    .map(changeStreamEvent -> {
                        switch (Objects.requireNonNull(changeStreamEvent.getOperationType())) {
                            case INSERT:
                            case UPDATE:
                                return new ProductEvent(ZonedDateTime.ofInstant(Objects.requireNonNull(changeStreamEvent.getTimestamp()),
                                        ZoneId.systemDefault()),
                                        Objects.requireNonNull(changeStreamEvent.getOperationType()).getValue(),
                                        changeStreamEvent.getBody(),
                                        Objects.requireNonNull(changeStreamEvent.getBody()).getId());
                            default:
                                return new ProductEvent(ZonedDateTime.ofInstant(Objects.requireNonNull(changeStreamEvent.getTimestamp()),
                                        ZoneId.systemDefault()),
                                        Objects.requireNonNull(changeStreamEvent.getOperationType()).getValue(),
                                        changeStreamEvent.getBody(),
                                        Objects.requireNonNull(changeStreamEvent.getRaw()).getDocumentKey().toString()
                                );
                        }
                    });
        }
        return changeStreamFlux;
    }
}
