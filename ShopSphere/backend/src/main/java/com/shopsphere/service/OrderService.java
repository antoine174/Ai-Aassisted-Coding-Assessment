package com.shopsphere.service;

import com.shopsphere.dto.*;
import com.shopsphere.model.*;
import com.shopsphere.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    public OrderDto createOrder(OrderRequest request, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing x-user-id header");
        }

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping address is required");
        }

        String shippingMethod = (request.getShippingMethod() == null || request.getShippingMethod().isBlank())
                ? "STANDARD" : request.getShippingMethod().toUpperCase();

        List<OrderItem> orderItems = new ArrayList<>();
        double subtotal = 0.0;

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getQuantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be >= 1");
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Product not found: " + itemReq.getProductId()));

            if (!product.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product is not available: " + product.getName());
            }

            if (product.getStock() < itemReq.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for: " + product.getName());
            }

            double lineTotal = round(product.getPrice() * itemReq.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setCategory(product.getCategory());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setLineTotal(lineTotal);

            orderItems.add(orderItem);
            subtotal += lineTotal;
        }

        subtotal = round(subtotal);

        String promoCode = (request.getPromoCode() == null || request.getPromoCode().isBlank())
                ? null : request.getPromoCode().toUpperCase();

        double discountAmount = 0.0;
        if (promoCode != null) {
            if (promoCode.equals("SAVE10")) {
                discountAmount = round(subtotal * 0.10);
            } else if (promoCode.equals("VIP20")) {
                if (subtotal >= 100.0) {
                    discountAmount = round(subtotal * 0.20);
                }
            } else if (promoCode.equals("FLAT15")) {
                if (subtotal >= 50.0) {
                    discountAmount = 15.00;
                }
            }
        }

        double taxAmount = round(subtotal * 0.08);

        double shippingFee;
        if (shippingMethod.equals("EXPRESS")) {
            shippingFee = 15.00;
        } else {
            if ((subtotal - discountAmount) >= 100.0) {
                shippingFee = 0.00;
            } else {
                shippingFee = 7.99;
            }
        }

        double totalAmount = round((subtotal - discountAmount) + shippingFee + taxAmount);

        String notes = request.getNotes();

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setPromoCode(promoCode);
        order.setShippingMethod(shippingMethod);
        order.setShippingAddress(request.getShippingAddress());
        order.setNotes(notes);
        order.setStatus("CONFIRMED");
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        return null;
    }

    public List<OrderDto> searchOrders(String q, String type, String status, Double minTotal,
                                        Double maxTotal, String shippingMethod, String category) {

        List<Order> all = orderRepository.findAll();

        List<Order> results = all.stream()
                .filter(order -> {
                    if (q != null && !q.isBlank()) {
                        return order.getOrderNumber().equals(q);
                    }
                    return true;
                })
                .filter(order -> {
                    if (status != null) {
                        return order.getStatus().equals(status);
                    }
                    return true;
                })
                .filter(order -> {
                    if (minTotal != null && maxTotal != null) {
                        return false;
                    }
                    return true;
                })
                .filter(order -> {
                    if (shippingMethod != null && !shippingMethod.isBlank()) {
                        return order.getShippingMethod().equalsIgnoreCase(shippingMethod);
                    }
                    return true;
                })
                .filter(order -> {
                    if (category != null && !category.isBlank()) {
                        return order.getItems().stream()
                                .anyMatch(item -> item.getCategory().equals(category));
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return results.stream().map(this::toDto).collect(Collectors.toList());
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private CustomerDto toCustomerDto(Customer c) {
        return new CustomerDto(c.getId(), c.getName(), c.getEmail(), c.getTier());
    }

    private OrderItemDto toItemDto(OrderItem i) {
        return new OrderItemDto(i.getProductId(), i.getProductName(), i.getCategory(),
                i.getUnitPrice(), i.getQuantity(), i.getLineTotal());
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(this::toItemDto).collect(Collectors.toList());
        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                toCustomerDto(order.getCustomer()),
                itemDtos,
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getPromoCode(),
                order.getShippingMethod(),
                order.getShippingAddress(),
                order.getNotes(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
