package com.example.querydsl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class ProductSearchCriteria {
    private UUID categoryId;    // nếu != null, lọc theo category
    private Double minPrice;    // nếu != null, price >= minPrice
    private Double maxPrice;    // nếu != null, price <= maxPrice
    private String keyword;     // nếu != null, tên product chứa keyword
}
