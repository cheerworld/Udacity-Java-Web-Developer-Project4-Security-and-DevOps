package com.example.demo.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	public static final Logger log = LoggerFactory.getLogger(UserController.class);

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		try {
			Optional<User> user = userRepository.findById(id);
			if(user.isPresent()) {
				log.info("User found with id: {}", id);
				return ResponseEntity.ok(user.get());
			} else {
				log.warn("User not found with id: {}", id);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log.error("Exception occurred while finding user by id: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		try {
			User user = userRepository.findByUsername(username);
			if (user == null) {
				log.warn("User not found with username: {}", username);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			} else {
				log.info("User found with username: {}", username);
				return ResponseEntity.ok(user);
			}
		} catch (Exception e) {
			log.error("Exception occurred while finding user by username: {}", username, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		try {
			User user = new User();
			user.setUsername(createUserRequest.getUsername());
			Cart cart = new Cart();
			cartRepository.save(cart);
			user.setCart(cart);
			if(createUserRequest.getPassword().length() < 7 ||
					!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())){
				log.error("CreateUser request failed: passwords do not match or length is less than 7 for username: {}", createUserRequest.getUsername());
				return ResponseEntity.badRequest().build();
			}
			user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
			userRepository.save(user);
			log.info("User created successfully: {}", createUserRequest.getUsername());
			return ResponseEntity.ok(user);
		} catch (Exception e) {
			log.error("Exception occurred while creating user: {}", createUserRequest.getUsername(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleExceptions(Exception e) {
		log.error("An unexpected error occurred: ", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
	}
}
