package com.demo.mongodbchangestreamspoc.repo;

import com.demo.mongodbchangestreamspoc.domain.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepo extends ReactiveMongoRepository<Product, String> {

}
