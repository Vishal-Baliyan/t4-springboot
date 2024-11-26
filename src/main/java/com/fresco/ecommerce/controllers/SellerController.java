package com.fresco.ecommerce.controllers;

import com.fresco.ecommerce.config.JwtUtil;
import com.fresco.ecommerce.models.User;
import com.fresco.ecommerce.models.Category;
import com.fresco.ecommerce.repo.ProductRepo;
import com.fresco.ecommerce.repo.CategoryRepo;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fresco.ecommerce.models.Product;

import java.util.Optional;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/api/auth/seller")
public class SellerController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private ProductRepo productRepo;
	
	@Autowired
	private CategoryRepo categoryRepo;

	@PostMapping("/product")
	@Transactional
	public ResponseEntity<Object> postProduct(@RequestHeader(name = "JWT") String jwt, @RequestBody Product product)  {
		product.setSeller(jwtUtil.getUser(jwt));
		Optional<Category> catEntity = categoryRepo.findByCategoryName(product.getCategory().getCategoryName());
		Category cat = catEntity.get();
		product.setCategory(cat);
		Product response = productRepo.save(product);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.LOCATION, "http://localhost/api/auth/seller/product/"+response.getProductId());
		return ResponseEntity.status(HttpStatus.CREATED).headers(headers).build();
		//  + response.getProductId()


		/*
			product.setSeller(jwtUtil.getUser(jwt));
		Category category = product.getCategory();
		category.setCategoryId(2);
		categoryRepo.save(category);
		Product response = productRepo.save(product);
		return ResponseEntity.status(HttpStatus.CREATED).body("http://localhost/api/auth/seller/product/" + response.getProductId());
		*/
	}

	@GetMapping("/product")
	public ResponseEntity<Object> getAllProducts(@RequestHeader(name = "JWT") String jwt) {
		User user = jwtUtil.getUser(jwt);
		Integer userId = user.getUserId();
		return ResponseEntity.ok(productRepo.findBySellerUserId(userId));
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<Object> getProduct(@RequestHeader(name = "JWT") String jwt, @PathVariable(name = "productId") Integer productId) {
		User user = jwtUtil.getUser(jwt);
		Integer userId = user.getUserId();
		Optional<Product> pro = productRepo.findBySellerUserIdAndProductId(userId, productId);
		if(!pro.isPresent()) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
		}
		return ResponseEntity.ok(productRepo.findById(Integer.valueOf(productId)));
	}

	@PutMapping("/product")
	public ResponseEntity<Object> putProduct(@RequestHeader(name = "JWT") String jwt, @RequestBody Product product) {
		Product newProduct = new Product();
		Optional<Product> pro = productRepo.findById(product.getProductId());
		if(pro.isPresent())
			newProduct = pro.get();
		else {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
		}
		// product.setSeller(jwtUtil.getUser(jwt));
		Optional<Category> catEntity = categoryRepo.findByCategoryName(product.getCategory().getCategoryName());
		Category cat = catEntity.get();
		newProduct.setCategory(cat);
		newProduct.setPrice(product.getPrice());
		newProduct.setProductName(product.getProductName());
		Product response = productRepo.saveAndFlush(newProduct);
		return ResponseEntity.ok(HttpStatus.OK);
	}

	@DeleteMapping("/product/{productId}")
	public ResponseEntity<Object> deleteProduct(@RequestHeader(name = "JWT") String jwt, @PathVariable(name = "productId") Integer productId) {
//		Product newProduct = new Product();
//		Optional<Product> pro = productRepo.findById(productId);
//		if(pro.isPresent())
//			newProduct = pro.get();
//		else {
//			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
//		}
		Optional<Product> pro = productRepo.findById(productId);
		if(!pro.isPresent()) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
		}
		productRepo.deleteById(productId);
		return ResponseEntity.ok(HttpStatus.OK);
	}
}