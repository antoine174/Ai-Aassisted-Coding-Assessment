package com.shopsphere.controller;

import com.shopsphere.dto.OrderDto;
import com.shopsphere.dto.OrderRequest;
import com.shopsphere.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestHeader(value = "x-user-id", required = false) String userId,
            @RequestBody OrderRequest request) {
        OrderDto created = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderDto>> searchOrders(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minTotal,
            @RequestParam(required = false) Double maxTotal,
            @RequestParam(required = false) String shippingMethod,
            @RequestParam(required = false) String category) {
        List<OrderDto> results = orderService.searchOrders(q, type, status, minTotal, maxTotal, shippingMethod, category);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        return orderService.searchOrders(null, null, null, null, null, null, null)
                .stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
