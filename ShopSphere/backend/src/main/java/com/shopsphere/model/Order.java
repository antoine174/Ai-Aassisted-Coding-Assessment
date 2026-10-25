package com.shopsphere.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double discountAmount;

    @Column(nullable = false)
    private double shippingFee;

    @Column(nullable = false)
    private double taxAmount;

    @Column(nullable = false)
    private double totalAmount;

    private String promoCode;

    @Column(nullable = false)
    private String shippingMethod;

    @Column(nullable = false)
    private String shippingAddress;

    private String notes;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
