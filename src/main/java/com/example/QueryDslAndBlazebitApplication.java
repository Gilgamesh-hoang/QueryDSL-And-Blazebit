package com.example;

import com.example.blaze.Blaze;
import com.example.querydsl.ProductSearchCriteria;
import com.example.querydsl.QueryDsl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QueryDslAndBlazebitApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryDslAndBlazebitApplication.class, args);
    }

    @Autowired
    QueryDsl dsl;

    @Autowired
    Blaze blaze;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            runBlazeQuery();

            runDslQuery();
        };
    }

    void runBlazeQuery() {

        blaze.keySetPaginationExample();

        blaze.searchProductsWithBuilder(
                ProductSearchCriteria.builder()
                        .categoryId(null)
                        .minPrice(1000.0)
                        .maxPrice(148000.0)
                        .keyword("Tinh Dầu Thơm")
                        .build()
        );

        blaze.findProductsWithCategory();

        blaze.findProductsAboveAveragePrice();

        blaze.countCategoriesWithMinProducts(14);

        blaze.groupAndCountProductByCategory();

        blaze.findAllActiveCategories(2,10);

        blaze.searchByNameCategory("cây");

        blaze.findAndSortCategories();

    }

    void runDslQuery() {
        dsl.findProductsWithCategory();


        dsl.findProductsAboveAveragePrice();

        dsl.countCategoriesWithMinProducts(14);

        dsl.groupAndCountProductByCategory();


        dsl.findAllActiveCategories(0, 6);
        System.out.println("=====");
        dsl.findAllActiveCategories(1, 6);


        dsl.searchByNameCategory("gỗ");
    }
}
