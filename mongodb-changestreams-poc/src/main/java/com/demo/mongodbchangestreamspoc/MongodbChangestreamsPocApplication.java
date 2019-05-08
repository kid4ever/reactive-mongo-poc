package com.demo.mongodbchangestreamspoc;

import com.demo.mongodbchangestreamspoc.domain.Product;
import com.demo.mongodbchangestreamspoc.repo.ProductRepo;
import com.demo.mongodbchangestreamspoc.service.ChangeStreamService;
import com.demo.mongodbchangestreamspoc.service.WebsocketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class MongodbChangestreamsPocApplication {

    private static final Integer INITIAL_COLLECTION_SIZE = 100;
    private static final Integer OPERATION_DELAY = 3;

    public static void main(String[] args) {
        SpringApplication.run(MongodbChangestreamsPocApplication.class, args);
    }


    /**
     * A runner that gets called by Spring Boot after Context load.
     * Populates the mongo collection with some initial entries.
     * Afterwards, continuously performs random inserts/updates/deletes every few seconds
     * to simulate database changes.
     * @param productRepo
     * @param mongoTemplate
     * @return
     */
    @Bean
    CommandLineRunner init(ProductRepo productRepo, ReactiveMongoTemplate mongoTemplate) {
        List<Product> productList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < INITIAL_COLLECTION_SIZE; i++) {
            productList.add(new Product(null, "Product" + i, ThreadLocalRandom.current().nextDouble(0.1, 20)));
        }
        return args -> {
            Flux<Product> productFlux = Flux.fromIterable(productList)
                    .flatMap(productRepo::save);

            // clean up collection and insert some new initial records.
            mongoTemplate.collectionExists(Product.class)
                    .flatMap(exists -> exists ? productRepo.deleteAll() : Mono.just(exists))
                    .thenMany(productFlux)
                    .thenMany(productRepo.findAll())
                    .subscribe(System.out::println);

            // perform a new random operation every 3 seconds
            Flux.interval(Duration.ofSeconds(OPERATION_DELAY))
                    .flatMap(val -> {
                        int randomInt = random.nextInt(3);
                        int randId = random.nextInt();
                        switch (randomInt % 3) {
                            case 0:
                                return Mono
                                        .just(new Product(null, "Product" + randId, ThreadLocalRandom.current().nextDouble(0.1, 20)))
                                        .flatMapMany(productRepo::save);
                            case 1:
                                return mongoTemplate
                                        .aggregate(Aggregation.newAggregation(Product.class, Aggregation.sample(1)), Product.class)
                                        .flatMap(product -> productRepo.findById(product.getId())
                                                .flatMap(existingProduct -> {
                                                    existingProduct.setPrice(product.getPrice() + 0.1);
                                                    return productRepo.save(existingProduct);
                                                }));
                            default:
                                return mongoTemplate
                                        .aggregate(Aggregation.newAggregation(Product.class, Aggregation.sample(1)), Product.class)
                                        .flatMap(product -> productRepo.findById(product.getId())
                                                .flatMap(productRepo::delete));
                        }
                    })
                    .log()
                    .subscribe();
        };
    }

    /**
     * Runner that continuously listens to the ChangeStream and publishes Product changes on a Websocket topic
     * @param changeStreamService
     * @param wsService
     * @return
     */
    @Bean
    CommandLineRunner initWebsocket(ChangeStreamService changeStreamService, WebsocketService wsService) {
        return args -> changeStreamService.productChangeEvents()
                .map(event -> {
                    wsService.publishEvent(event);
                    return event;
                })
                .subscribe();
    }
}
