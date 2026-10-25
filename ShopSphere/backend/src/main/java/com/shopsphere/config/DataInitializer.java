package com.shopsphere.config;

import com.shopsphere.model.*;
import com.shopsphere.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) return;

        Customer alice = new Customer("user-1", "Alice Vance", "alice@example.com", "STANDARD");
        Customer bob = new Customer("user-2", "Bob Sterling", "bob@prime.com", "PRIME");
        customerRepository.saveAll(List.of(alice, bob));

        Product p1 = new Product("prod-1", "Wireless Noise-Cancelling Headphones", "Electronics", 120.00, 15, true);
        Product p2 = new Product("prod-2", "Mechanical Gaming Keyboard", "Electronics", 80.00, 10, true);
        Product p3 = new Product("prod-3", "Ergonomic Office Chair", "Furniture", 250.00, 5, true);
        Product p4 = new Product("prod-4", "Clean Architecture Book", "Books", 40.00, 20, true);
        Product p5 = new Product("prod-5", "USB-C Hub Adapter", "Accessories", 25.00, 2, true);
        Product p6 = new Product("prod-6", "Legacy Discontinued Monitor", "Electronics", 199.99, 8, false);
        productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));

        OrderItem item101a = new OrderItem(null, "prod-1", "Wireless Noise-Cancelling Headphones", "Electronics", 120.00, 1, 120.00);
        OrderItem item101b = new OrderItem(null, "prod-4", "Clean Architecture Book", "Books", 40.00, 1, 40.00);
        Order ord101 = new Order(null, "ord-101", alice, List.of(item101a, item101b),
                160.00, 0.00, 0.00, 12.80, 172.80, null,
                "STANDARD", "742 Evergreen Terrace, Springfield", "Standard delivery to 742 Evergreen Terrace, Springfield",
                "CONFIRMED", Instant.parse("2026-01-10T10:00:00Z"), Instant.parse("2026-01-10T10:00:00Z"));
        orderRepository.save(ord101);

        OrderItem item102 = new OrderItem(null, "prod-3", "Ergonomic Office Chair", "Furniture", 250.00, 1, 250.00);
        Order ord102 = new Order(null, "ord-102", bob, List.of(item102),
                250.00, 0.00, 15.00, 20.00, 285.00, null,
                "EXPRESS", "100 Main St, Seattle", "Express delivery to 100 Main St, Seattle",
                "SHIPPED", Instant.parse("2026-01-12T12:00:00Z"), Instant.parse("2026-01-12T12:00:00Z"));
        orderRepository.save(ord102);

        OrderItem item103 = new OrderItem(null, "prod-2", "Mechanical Gaming Keyboard", "Electronics", 80.00, 2, 160.00);
        Order ord103 = new Order(null, "ord-103", alice, List.of(item103),
                160.00, 0.00, 0.00, 12.80, 172.80, null,
                "STANDARD", "742 Evergreen Terrace, Springfield", "Standard delivery to 742 Evergreen Terrace, Springfield",
                "DELIVERED", Instant.parse("2026-01-15T15:30:00Z"), Instant.parse("2026-01-15T15:30:00Z"));
        orderRepository.save(ord103);

        OrderItem item104 = new OrderItem(null, "prod-5", "USB-C Hub Adapter", "Accessories", 25.00, 1, 25.00);
        Order ord104 = new Order(null, "ord-104", bob, List.of(item104),
                25.00, 0.00, 0.00, 2.00, 27.00, null,
                "STANDARD", "500 Pine Ave, Austin", "Standard delivery to 500 Pine Ave, Austin",
                "CONFIRMED", Instant.parse("2026-01-18T09:00:00Z"), Instant.parse("2026-01-18T09:00:00Z"));
        orderRepository.save(ord104);

        OrderItem item105 = new OrderItem(null, "prod-1", "Wireless Noise-Cancelling Headphones", "Electronics", 120.00, 1, 120.00);
        Order ord105 = new Order(null, "ord-105", alice, List.of(item105),
                120.00, 0.00, 0.00, 9.60, 129.60, null,
                "STANDARD", "742 Evergreen Terrace, Springfield", "Standard delivery to 742 Evergreen Terrace, Springfield",
                "CANCELLED", Instant.parse("2026-01-20T11:00:00Z"), Instant.parse("2026-01-20T11:00:00Z"));
        orderRepository.save(ord105);
    }
}
