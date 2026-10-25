package com.shopsphere.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    @JsonProperty("_id")
    private String id;

    private String orderNumber;
    private CustomerDto customer;
    private List<OrderItemDto> items;
    private double subtotal;
    private double discountAmount;
    private double shippingFee;
    private double taxAmount;
    private double totalAmount;
    private String promoCode;
    private String shippingMethod;
    private String shippingAddress;
    private String notes;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
