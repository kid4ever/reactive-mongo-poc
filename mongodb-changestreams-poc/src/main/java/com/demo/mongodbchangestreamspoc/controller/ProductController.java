package com.demo.mongodbchangestreamspoc.controller;

import com.demo.mongodbchangestreamspoc.domain.Product;
import com.demo.mongodbchangestreamspoc.domain.ProductEvent;
import com.demo.mongodbchangestreamspoc.repo.ProductRepo;
import com.demo.mongodbchangestreamspoc.service.ChangeStreamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductRepo productRepo;
    private ChangeStreamService changeStreamService;

    public ProductController(ProductRepo productRepo, ChangeStreamService changeStreamService) {
        this.productRepo = productRepo;
        this.changeStreamService = changeStreamService;
    }

    @GetMapping
    public Flux<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
        return productRepo.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> saveProduct(@RequestBody Product product) {
        return productRepo.save(product);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return productRepo.findById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    return productRepo.save(existingProduct);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        return productRepo.findById(id)
                .flatMap(existingProduct ->
                        productRepo.delete(existingProduct)
                                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public Mono<Void> deleteAllProducts() {
        return productRepo.deleteAll();
    }

    /**
     * GET endpoint for tapping into a stream of server side events
     * @return A Stream of Product Events
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> getProductEvents() {

        return changeStreamService.productChangeEvents();

    }
}
