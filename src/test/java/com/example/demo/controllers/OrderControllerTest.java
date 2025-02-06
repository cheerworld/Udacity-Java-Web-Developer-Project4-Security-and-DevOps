package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class OrderControllerTest {
    private OrderController orderController;
    private final UserRepository userRepo = mock(UserRepository.class);
    private final OrderRepository orderRepo = mock(OrderRepository.class);

    @BeforeEach
    public void setUp() {
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepo);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepo);
    }

    @Test
    public void submit_happy_path() {
        User user = createUser();
        when(userRepo.findByUsername("test")).thenReturn(user);

        final ResponseEntity<UserOrder> response = orderController.submit("test");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserOrder order = response.getBody();
        assertNotNull(order);
        assertEquals(user.getCart().getItems().size(), order.getItems().size());
        assertEquals(user.getCart().getTotal(), order.getTotal());
        assertEquals(user, order.getUser());
    }

    @Test
    public void submit_user_not_found() {
        when(userRepo.findByUsername("test")).thenReturn(null);

        final ResponseEntity<UserOrder> response = orderController.submit("test");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getOrdersForUser_happy_path() {
        User user = createUser();
        when(userRepo.findByUsername("test")).thenReturn(user);
        when(orderRepo.findByUser(user)).thenReturn(Collections.singletonList(createOrder(user)));

        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserOrder> orders = response.getBody();
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(user, orders.get(0).getUser());
    }

    @Test
    public void getOrdersForUser_user_not_found() {
        when(userRepo.findByUsername("test")).thenReturn(null);

        final ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("test");
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
        cart.setItems(Collections.emptyList());
        cart.setTotal(BigDecimal.valueOf(0.0));

        user.setCart(cart);
        return user;
    }

    private UserOrder createOrder(User user) {
        UserOrder order = new UserOrder();
        order.setId(0L);
        order.setUser(user);
        order.setItems(user.getCart().getItems());
        order.setTotal(user.getCart().getTotal());
        return order;
    }
}

