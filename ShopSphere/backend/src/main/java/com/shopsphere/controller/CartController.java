package com.shopsphere.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "items", List.of(),
                "total", 0.0
        ));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable String userId,
            @RequestBody Map<String, Object> item) {
        return ResponseEntity.ok(Map.of("message", "Item added to cart", "userId", userId));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {
        return ResponseEntity.noContent().build();
    }
}
