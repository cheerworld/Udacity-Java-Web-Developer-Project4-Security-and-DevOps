package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class CartControllerTest {

    private CartController cartController;
    private final UserRepository userRepo = mock(UserRepository.class);
    private final CartRepository cartRepo = mock(CartRepository.class);
    private final ItemRepository itemRepo = mock(ItemRepository.class);

    @BeforeEach
    public void setUp() {
        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);
    }

    @Test
    public void addTocart_happy_path() {
        User user = createUser();
        Item item = createItem();
        ModifyCartRequest request = createModifyCartRequest("test", 1L, 2);
        when(userRepo.findByUsername("test")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        final ResponseEntity<Cart> response = cartController.addTocart(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Cart cart = response.getBody();
        assertNotNull(cart);
        assertEquals(2, cart.getItems().size());
        assertEquals(BigDecimal.valueOf(20.0), cart.getTotal());
    }

    @Test
    public void addTocart_user_not_found() {
        ModifyCartRequest request = createModifyCartRequest("test", 1L, 2);
        when(userRepo.findByUsername("test")).thenReturn(null);

        final ResponseEntity<Cart> response = cartController.addTocart(request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void addTocart_item_not_found() {
        User user = createUser();
        ModifyCartRequest request = createModifyCartRequest("test", 1L, 2);
        when(userRepo.findByUsername("test")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.empty());

        final ResponseEntity<Cart> response = cartController.addTocart(request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void removeFromcart_happy_path() {
        User user = createUser();
        Item item = createItem();
        ModifyCartRequest request = createModifyCartRequest("test", 1L, 1);
        when(userRepo.findByUsername("test")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.of(item));

        user.getCart().addItem(item); // Add the item to the cart initially
        final ResponseEntity<Cart> response = cartController.removeFromcart(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Cart cart = response.getBody();
        assertNotNull(cart);
        assertEquals(0, cart.getItems().size());
        assertEquals(0, cart.getTotal().compareTo(BigDecimal.ZERO)); // Use compareTo for BigDecimal comparison
    }


    @Test
    public void removeFromcart_user_not_found() {
        ModifyCartRequest request = createModifyCartRequest("test", 1L, 1);
        when(userRepo.findByUsername("test")).thenReturn(null);

        final ResponseEntity<Cart> response = cartController.removeFromcart(request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void removeFromcart_item_not_found() {
        User user = createUser();
        ModifyCartRequest request = createModifyCartRequest("test", 1L, 1);
        when(userRepo.findByUsername("test")).thenReturn(user);
        when(itemRepo.findById(1L)).thenReturn(Optional.empty());

        final ResponseEntity<Cart> response = cartController.removeFromcart(request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private User createUser() {
        User user = new User();
        user.setId(0L);
        user.setUsername("test");
        user.setPassword("thisIsHashed");

        Cart cart = new Cart();
        cart.setId(0L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());  // Initialize with a modifiable list
        cart.setTotal(BigDecimal.ZERO);

        user.setCart(cart);
        return user;
    }

    private Item createItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Item1");
        item.setPrice(BigDecimal.valueOf(10.0));
        item.setDescription("Test Description");
        return item;
    }

    private ModifyCartRequest createModifyCartRequest(String username, long itemId, int quantity) {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(username);
        request.setItemId(itemId);
        request.setQuantity(quantity);
        return request;
    }
}
