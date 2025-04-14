package com.david.springcloud.msvc.items.services;

import com.david.springcloud.msvc.items.models.Item;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    List<Item> findAll();

    Optional<Item> findById(Long id);
}
