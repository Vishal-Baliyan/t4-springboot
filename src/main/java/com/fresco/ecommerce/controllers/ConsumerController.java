package com.fresco.ecommerce.controllers;

import com.fresco.ecommerce.config.JwtUtil;
import com.fresco.ecommerce.models.Category;
import com.fresco.ecommerce.models.Cart;
import com.fresco.ecommerce.models.CartProduct;
import com.fresco.ecommerce.models.Product;
import com.fresco.ecommerce.models.User;
import com.fresco.ecommerce.repo.CartRepo;
import com.fresco.ecommerce.repo.CategoryRepo;
import com.fresco.ecommerce.repo.CartProductRepo;
import com.fresco.ecommerce.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api/auth/consumer")
public class ConsumerController {

	@Autowired
	private CartRepo cartRepo;
	
	@Autowired
	private CartProductRepo cartProductRepo;
	
	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private JwtUtil jwtUtil;


	@GetMapping("/cart")
	public ResponseEntity<Object> getCart(@RequestHeader(name = "JWT") String jwt) {
		String username= jwtUtil.getUser(jwt).getUsername();
		return ResponseEntity.ok(cartRepo.findByUserUsername(username));
	}

	@PostMapping("/cart")
	public ResponseEntity<Object> postCart(@RequestHeader(name = "JWT") String jwt, @RequestBody Product product) {
		product.setSeller(jwtUtil.getUser(jwt));
		Optional<Category> catEntity = categoryRepo.findByCategoryName(product.getCategory().getCategoryName());
		Category cat = catEntity.get();
		product.setCategory(cat);
		Integer productId = product.getProductId();
		User user = jwtUtil.getUser(jwt);
		Integer userId = user.getUserId();
		Optional<CartProduct> OptionalCp = cartProductRepo.findByCartUserUserIdAndProductProductId(userId, productId);
		if(OptionalCp.isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("already exist");
		}
		CartProduct cp = new CartProduct();
		cp.setProduct(product);
		cp.setQuantity(1);
		String username= jwtUtil.getUser(jwt).getUsername();
		Optional<Cart> cp1 = cartRepo.findByUserUsername(username);
		
		cp.setCart(cp1.get());
		
		CartProduct response = cartProductRepo.save(cp);
		Cart ca = cp1.get();
		Double val = product.getPrice();
		Double caVal = ca.getTotalAmount();
		ca.setTotalAmount(val + caVal);
		cartRepo.save(ca);
		return ResponseEntity.ok(HttpStatus.OK);
	}

	@PutMapping("/cart")
	public ResponseEntity<Object> putCart(@RequestHeader(name = "JWT") String jwt, @RequestBody CartProduct cartProduct) {
		CartProduct cp = cartProduct;
		Product product = cp.getProduct();
		Integer productId = product.getProductId();
		User user = jwtUtil.getUser(jwt);
		Integer userId = user.getUserId();

		Integer quantity = cp.getQuantity();

		if(quantity == 0) {
			cartProductRepo.deleteByCartUserUserIdAndProductProductId(userId, productId);
		} else {
			Optional<CartProduct> OptionalCp = cartProductRepo.findByCartUserUserIdAndProductProductId(userId, productId);
			CartProduct c = OptionalCp.get();
			c.setQuantity(quantity);
			c.setProduct(product);
			String username= jwtUtil.getUser(jwt).getUsername();
			Optional<Cart> cp1 = cartRepo.findByUserUsername(username);
			Cart cart = cp1.get();
			c.setCart(cart);
			cartProductRepo.save(c);
			
		}

		return ResponseEntity.ok(HttpStatus.OK);
	}

	@DeleteMapping("/cart")
	public ResponseEntity<Object> deleteCart(@RequestHeader(name = "JWT") String jwt, @RequestBody Product product) {
		Integer productId = product.getProductId();
		User user = jwtUtil.getUser(jwt);
		Integer userId = user.getUserId();

		cartProductRepo.deleteByCartUserUserIdAndProductProductId(userId, productId);

		return ResponseEntity.ok(HttpStatus.OK);
	}

}