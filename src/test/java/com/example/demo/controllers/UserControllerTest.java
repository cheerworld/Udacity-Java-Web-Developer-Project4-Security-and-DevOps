package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    private static final Logger log = LoggerFactory.getLogger(UserControllerTest.class);

    private UserController userController;
    private final UserRepository userRepo = mock(UserRepository.class);
    private final CartRepository cartRepo = mock(CartRepository.class);
    private final BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    @BeforeEach
    public void setUp() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
    }

    @Test
    public void create_user_happy_path() {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");
        request.setPassword("testPassword");
        request.setConfirmPassword("testPassword");
        final ResponseEntity<User> response = userController.createUser(request);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        User user = response.getBody();
        assertNotNull(user);
        assertEquals("test", user.getUsername());
        assertEquals("thisIsHashed", user.getPassword());
        log.info("User created successfully: {}", user.getUsername());
    }

    @Test
    public void create_user_password_too_short() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");
        request.setPassword("short");
        request.setConfirmPassword("short");
        final ResponseEntity<User> response = userController.createUser(request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        log.error("CreateUser request failed: passwords do not match or length is less than 7 for username: {}", request.getUsername());
    }

    @Test
    public void create_user_passwords_do_not_match() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");
        request.setPassword("testPassword");
        request.setConfirmPassword("differentPassword");
        final ResponseEntity<User> response = userController.createUser(request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        log.error("CreateUser request failed: passwords do not match or length is less than 7 for username: {}", request.getUsername());
    }

    @Test
    public void findById_happy_path() {
        User user = createUser();
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        final ResponseEntity<User> response = userController.findById(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test", response.getBody().getUsername());
        log.info("User found with id: {}", 1L);
    }

    @Test
    public void findById_not_found() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        final ResponseEntity<User> response = userController.findById(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        log.warn("User not found with id: {}", 1L);
    }

    @Test
    public void findByUserName_happy_path() {
        User user = createUser();
        when(userRepo.findByUsername("test")).thenReturn(user);

        final ResponseEntity<User> response = userController.findByUserName("test");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test", response.getBody().getUsername());
        log.info("User found with username: {}", "test");
    }

    @Test
    public void findByUserName_not_found() {
        when(userRepo.findByUsername("test")).thenReturn(null);

        final ResponseEntity<User> response = userController.findByUserName("test");
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        log.warn("User not found with username: {}", "test");
    }

    private User createUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("thisIsHashed");

        Cart cart = new Cart();
        cart.setId(0L);
        cart.setUser(user);
        user.setCart(cart);

        return user;
    }
}
