package com.shopsphere.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private String shippingMethod;
    private String promoCode;
    private String notes;
}
