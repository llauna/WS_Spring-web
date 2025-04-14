package com.david.springcloud.msvc.items.controllers;

import com.david.springcloud.msvc.items.models.Item;
import com.david.springcloud.msvc.items.services.ItemService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

@RestController
public class ItemController {
    
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<Item> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Item> itemOptional = service.findById(id);
        if (itemOptional.isPresent()) {
            return ResponseEntity.ok(itemOptional.get());
        }
        return ResponseEntity.status(404)
                .body(Collections.singletonMap (
                        "message", "No existe el producto en el microservicio msvc-products"
                ));
    }
}
