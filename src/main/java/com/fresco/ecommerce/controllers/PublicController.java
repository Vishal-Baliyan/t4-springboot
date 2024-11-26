package com.fresco.ecommerce.controllers;

import java.util.List;

import com.fresco.ecommerce.config.JwtUtil;
import com.fresco.ecommerce.models.User;
import com.fresco.ecommerce.repo.ProductRepo;
import com.fresco.ecommerce.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fresco.ecommerce.models.Product;

@RestController
@RequestMapping("/api/public")
public class PublicController {

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private UserAuthService userAuthService;

	@Autowired
	private JwtUtil jwtUtil;

	@GetMapping("/product/search")
	public List<Product> getProducts(@RequestParam String keyword) {
		return productRepo.findByProductNameContainingIgnoreCaseOrCategoryCategoryNameContainingIgnoreCase(keyword, keyword);
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody User requestBody) {
		try {
			User user = userAuthService.loadUserByUsername(requestBody.getUsername());
			if(user.getPassword().equals(requestBody.getPassword())) {
				return ResponseEntity.ok(jwtUtil.generateToken(user));
			}
		}catch (Exception e) {
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

	}



}