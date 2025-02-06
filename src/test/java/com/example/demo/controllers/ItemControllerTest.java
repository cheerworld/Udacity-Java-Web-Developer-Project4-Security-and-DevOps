package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ItemControllerTest {

    private ItemController itemController;
    private final ItemRepository itemRepo = mock(ItemRepository.class);

    @BeforeEach
    public void setUp() {
        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepo);
    }

    @Test
    public void getItems_happy_path() {
        Item item = createItem(1L, "Item1", BigDecimal.valueOf(10.0), "Test Description");
        when(itemRepo.findAll()).thenReturn(Collections.singletonList(item));

        final ResponseEntity<List<Item>> response = itemController.getItems();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Item> items = response.getBody();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Item1", items.get(0).getName());
    }

    @Test
    public void getItemById_happy_path() {
        Item item = createItem(1L, "Item1", BigDecimal.valueOf(10.0), "Test Description");
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        final ResponseEntity<Item> response = itemController.getItemById(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Item returnedItem = response.getBody();
        assertNotNull(returnedItem);
        assertEquals("Item1", returnedItem.getName());
    }

    @Test
    public void getItemById_not_found() {
        when(itemRepo.findById(1L)).thenReturn(Optional.empty());

        final ResponseEntity<Item> response = itemController.getItemById(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getItemsByName_happy_path() {
        Item item = createItem(1L, "Item1", BigDecimal.valueOf(10.0), "Test Description");
        when(itemRepo.findByName("Item1")).thenReturn(Collections.singletonList(item));

        final ResponseEntity<List<Item>> response = itemController.getItemsByName("Item1");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Item> items = response.getBody();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Item1", items.get(0).getName());
    }

    @Test
    public void getItemsByName_not_found() {
        when(itemRepo.findByName("Item1")).thenReturn(Collections.emptyList());

        final ResponseEntity<List<Item>> response = itemController.getItemsByName("Item1");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private Item createItem(Long id, String name, BigDecimal price, String description) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setPrice(price);
        item.setDescription(description);
        return item;
    }
}
