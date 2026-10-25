package com.shopsphere.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private String productId;
    private int quantity;
}
