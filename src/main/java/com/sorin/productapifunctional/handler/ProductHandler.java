package com.sorin.productapifunctional.handler;

import com.sorin.productapifunctional.model.Product;
import com.sorin.productapifunctional.model.ProductEvent;
import com.sorin.productapifunctional.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.rmi.ServerError;
import java.time.Duration;

@Component
public class ProductHandler {
    private ProductRepository productRepository;

    public ProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Mono<ServerResponse> getAllProducts(ServerRequest serverRequest) {
        Flux<Product> products = productRepository.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(products, Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Product> productMono = productRepository.findById(id);
        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(product)))
                .switchIfEmpty(notFound());
    }

    public Mono<ServerResponse> saveProduct(ServerRequest serverRequest) {
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);

        return productMono.flatMap(product ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(productRepository.save(product), Product.class));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Product> existingProductMono = productRepository.findById(id);
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);

        return productMono.zipWith(existingProductMono,
                (product, existingProduct) ->
                        new Product(existingProduct.getId(), product.getName(), product.getPrice()))
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productRepository.save(product), Product.class)
                ).switchIfEmpty(notFound());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Product> productMono = productRepository.findById(id);

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .build(productRepository.delete(product))
                                .switchIfEmpty(notFound()));
    }

    public Mono<ServerResponse> deleteProducts(ServerRequest serverRequest) {

        return ServerResponse.ok()
                .build(productRepository.deleteAll());
    }

    public Mono<ServerResponse> getProductEvents(ServerRequest serverRequest) {
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val -> new ProductEvent(val, "Product Event"));

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvent.class);
    }

    private Mono<ServerResponse> notFound() {
        return ServerResponse.notFound().build();
    }

}
