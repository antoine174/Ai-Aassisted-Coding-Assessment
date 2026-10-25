package com.shopsphere.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private String productId;
    private String productName;
    private String category;
    private double unitPrice;
    private int quantity;
    private double lineTotal;
}
