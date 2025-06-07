package com.example.querydsl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor

public class CategoryProductInfo {
    private String categoryName;
    private String productName;
    private Double price;

}
