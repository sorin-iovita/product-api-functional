package com.sorin.productapifunctional.repository;

import com.sorin.productapifunctional.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
