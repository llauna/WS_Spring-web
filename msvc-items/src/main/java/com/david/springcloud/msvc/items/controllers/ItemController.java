package com.david.springcloud.msvc.items.controllers;

import com.david.springcloud.msvc.items.models.Item;
import com.david.springcloud.msvc.items.models.Product;
import com.david.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@RestController
public class ItemController {

    private final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService service;
    private final CircuitBreakerFactory cBreakerFactory;


    public ItemController (@Qualifier("itemServiceWebClient") ItemService service,
                           CircuitBreakerFactory cBreakerFactory) {
        this.cBreakerFactory = cBreakerFactory;
        this.service = service;
    }

    @GetMapping
    public List<Item> list(
                            @RequestParam(name = "name", required = false) String name,
                            @RequestHeader(name="token-request", required = false) String token) {
        System.out.println("Name: " + name);
        System.out.println("Token: " + token);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Item> itemOptional = cBreakerFactory.create("items").run(() -> service.findById(id),
                e-> {
                    System.out.println(e.getMessage());
                    logger.error(e.getMessage());
                    Product product = new Product();
                    product.setCreateAt(LocalDate.now());
                    product.setId(1L);
                    product.setName("Camera Sony");
                    product.setPrice((double) 500.00);
                    product.setPort(0);
                    /* Item item = new Item(product, 5); */
                    return Optional.of(new Item(product, 5));
                });
        if (itemOptional.isPresent()) {
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
                .body(Collections.singletonMap (
                        "message", "No existe el producto en el microservicio msvc-products"
                ));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "getFallbackMethodProduct")
    @GetMapping("/details/{id}")
    public ResponseEntity<?> details2(@PathVariable Long id) {
        Optional<Item> itemOptional = service.findById(id);
        if (itemOptional.isPresent()) {
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
                .body(Collections.singletonMap (
                        "message",
                        "No existe el producto en el microservicio msvc-products"
                ));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "getFallbackMethodProduct")
    @TimeLimiter(name = "items")
    @GetMapping("/details2/{id}")
    public CompletableFuture<?> details3(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Item> itemOptional = service.findById(id);
            if (itemOptional.isPresent()) {
                return ResponseEntity.ok(itemOptional.get());
            }
            return ResponseEntity.status(404)
                    .body(Collections.singletonMap (
                            "message",
                            "No existe el producto en el microservicio msvc-products"
                    ));
        });
    }

    public ResponseEntity<?> getFallbackMethodProduct(Throwable e) {
        System.out.println(e.getMessage());
        logger.error(e.getMessage());

        Product product = new Product();
        product.setCreateAt(LocalDate.now());
        product.setId(1L);
        product.setName("Camera Sony");
        product.setPrice((double) 500.00);
        product.setPort(0);
        return ResponseEntity.ok(new Item(product, 5));
    }

    public CompletableFuture<?> getFallBackMethodProduct2(Throwable e) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(e.getMessage());
            logger.error(e.getMessage());

            Product product = new Product();
            product.setCreateAt(
                    LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony");
            product.setPrice(
                    500.00);
            return ResponseEntity.ok(new Item(product,
                    5));
        });
    }

}
