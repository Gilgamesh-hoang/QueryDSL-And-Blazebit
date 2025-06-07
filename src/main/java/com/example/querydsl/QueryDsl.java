package com.example.querydsl;

import com.example.Util;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.entity.QCategory;
import com.example.entity.QProduct;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class QueryDsl {
    JPAQueryFactory queryFactory;

    public void searchProductsWithChaining(ProductSearchCriteria criteria) {
        QProduct p = QProduct.product;
        QCategory c = QCategory.category;

        List<Product> products = queryFactory
                .selectFrom(p)
                .join(p.category, c)
                .where(
                        criteria.getCategoryId() != null ? p.category.id.eq(criteria.getCategoryId()) : null,
                        criteria.getMinPrice() != null ? p.price.goe(criteria.getMinPrice()) : null,
                        criteria.getMaxPrice() != null ? p.price.loe(criteria.getMaxPrice()) : null,
                        (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty())
                                ? p.name.containsIgnoreCase(criteria.getKeyword())
                                : null
                )
                .orderBy(p.createdAt.desc())
                .fetch();

        Util.printProduct(products);
    }

    public void searchProductsWithBooleanBuilder(ProductSearchCriteria criteria) {
        QProduct p = QProduct.product;
        QCategory c = QCategory.category;

        BooleanBuilder builder = new BooleanBuilder();
        if (criteria.getCategoryId() != null) {
            builder.and(c.id.eq(criteria.getCategoryId()));
        }

        // Lọc theo khoảng giá
        if (criteria.getMinPrice() != null) {
            builder.and(p.price.goe(criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            builder.and(p.price.loe(criteria.getMaxPrice()));
        }
        // Lọc theo từ khoá trong tên product
        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            builder.and(p.name.containsIgnoreCase(criteria.getKeyword()));
        }

        Util.printProduct(queryFactory.selectFrom(p)
                .innerJoin(p.category, c)
                .where(builder)
                .fetch()
        );

    }

    public void findProductsWithCategory() {
        QCategory category = QCategory.category;
        QProduct product = QProduct.product;

        List<CategoryProductInfo> data = queryFactory.select(
                        Projections.constructor(CategoryProductInfo.class, category.name, product.name, product.price)
                )
                .from(product)
                .innerJoin(product.category, category)
                .limit(10)
                .fetch();

        data.forEach(System.out::println);
    }

    public void findProductsAboveAveragePrice() {
        QProduct product = QProduct.product;
        QProduct p2 = new QProduct("p2"); // alias khác cho subquery

        List<Product> products = queryFactory.selectFrom(product)
                .where(product.price.goe(
                        JPAExpressions.select(p2.price.avg())
                                .from(p2)
                ))
                .fetch();
        Util.printProduct(products);

    }

    public void countCategoriesWithMinProducts(int minCount) {
        QCategory category = QCategory.category;
        QProduct product = QProduct.product;

        NumberExpression<Long> productCount = product.count();

        List<Tuple> data = queryFactory.select(category.name, productCount)
                .from(product)
                .innerJoin(product.category, category)
                .where(category.isDeleted.isFalse())
                .groupBy(category.id)
                .having(productCount.goe(minCount))
                .orderBy(productCount.desc())
                .fetch();

        data.forEach(tuple -> {
            String name = tuple.get(category.name);
            Optional<Long> count = Optional.ofNullable(tuple.get(product.count()));
            System.out.println("Category: " + name + ", Products: " + count.orElse(0L));
        });
    }


    public void groupAndCountProductByCategory() {
        QCategory category = QCategory.category;
        QProduct product = QProduct.product;

        List<Tuple> data = queryFactory.select(category.name, product.count())
                .from(product)
                .innerJoin(product.category, category)
                .groupBy(category.id)
                .fetch();

        data.forEach(tuple -> {
            String name = tuple.get(category.name);
            Optional<Long> count = Optional.ofNullable(tuple.get(product.count()));
            System.out.println("Category: " + name + ", Products: " + count.orElse(0L));
        });

    }

    //lấy danh sách tất cả các Category chưa bị xóa (isDeleted = false), sắp xếp theo createdAt giảm dần.
    public void findAllActiveCategories(int page, int size) {
        QCategory category = QCategory.category;

        List<Category> categories = queryFactory.selectFrom(category)
                .where(category.isDeleted.isFalse())
                .orderBy(category.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();

        Util.print(categories);

    }

    public void searchByNameCategory(String keyword) {
        QCategory category = QCategory.category;

        List<Tuple> data = queryFactory
                .select(category.id, category.name)
                .from(category)
                .where(
                        category.name.containsIgnoreCase(keyword)
                                .and(category.createdAt.after(Timestamp.valueOf("2025-04-08 0:0:0")))
                                .and(category.isDeleted.isFalse())
                ).fetch();

        List<Category> categories = data.stream().map(item ->
                Category.builder()
                        .name(item.get(category.name))
                        .id(item.get(category.id))
                        .build()
        ).toList();

        Util.print(categories);

    }


}
