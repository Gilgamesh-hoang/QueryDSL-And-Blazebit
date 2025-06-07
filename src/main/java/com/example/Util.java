package com.example;

import com.example.entity.Category;
import com.example.entity.Product;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class Util {
    public void printProduct(List<Product> products) {
        products.forEach(product -> System.out.println(
                "Product ID: " + product.getId() +
                        ", Name: " + product.getName() +
                        ", Price: " + product.getPrice() +
                        ", Category ID: " + product.getCategory().getId()
        ));
    }

    public void print(List<Category> categories) {
        categories.forEach(category -> System.out.println(
                "Category ID: " + category.getId() +
                        ", Name: " + category.getName() +
                        ", Created At: " + category.getCreatedAt() +
                        ", Is Deleted: " + category.isDeleted()
        ));
    }

}
