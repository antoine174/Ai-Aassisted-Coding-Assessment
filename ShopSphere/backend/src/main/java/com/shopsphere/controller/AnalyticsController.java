package com.shopsphere.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(Map.of(
                "totalOrders", 5,
                "totalRevenue", 787.20,
                "activeProducts", 5,
                "activeCustomers", 2
        ));
    }

    @GetMapping("/top-products")
    public ResponseEntity<Map<String, Object>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(Map.of("limit", limit, "products", java.util.List.of()));
    }
}
