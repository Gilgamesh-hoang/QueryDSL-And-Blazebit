package com.example.blaze;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.example.Util;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.querydsl.CategoryProductInfo;
import com.example.querydsl.ProductSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Blaze {
    final CriteriaBuilderFactory builderFactory;
    @PersistenceContext
    EntityManager em;


    public void keySetPaginationExample() {
        int pageSize = 5;

        // Trang 2: từ vị trí thứ 5 (index = 5)
        PagedList<Product> page2 = getProductsPage(null, 5, pageSize);
        System.out.println(">>> Page 2:");
        Util.printProduct(page2.stream().toList());

        KeysetPage keysetPage2 = page2.getKeysetPage();

        // Trang 3: từ keyset của page2, tương đương offset = 10
        PagedList<Product> page3 = getProductsPage(keysetPage2, 10, pageSize);
        System.out.println(">>> Page 3:");
        Util.printProduct(page3.stream().toList());

        // Trang 1: từ keyset của page2, nhưng offset = 0 → tìm previous
        PagedList<Product> page1 = getProductsPage(keysetPage2, 0, pageSize);
        System.out.println(">>> Page 1:");
        Util.printProduct(page1.stream().toList());
    }

    private PagedList<Product> getProductsPage(KeysetPage keysetPage, int offset, int limit) {
        return builderFactory.create(em, Product.class)
                .orderByAsc("createdAt")
                .orderByAsc("id")
                .page(keysetPage, offset, limit)
                .getResultList();
    }

    public void searchProductsWithBuilder(ProductSearchCriteria criteria) {
        CriteriaBuilder<Product> builder = builderFactory.create(em, Product.class)
                .from(Product.class, "p");

        if (criteria.getCategoryId() != null) {
            builder.where("p.category.id").eq(criteria.getCategoryId());
        }

        if (!criteria.getKeyword().isBlank()) {
            builder.where("p.name").like(false).value("%" + criteria.getKeyword() + "%").noEscape();
        }

        if (criteria.getMinPrice() != null) {
            builder.where("p.price").ge(criteria.getMinPrice());
        }

        if (criteria.getMaxPrice() != null) {
            builder.where("p.price").le(criteria.getMaxPrice());
        }

        Util.printProduct(builder.getResultList());
    }

    public void findProductsWithCategory() {
        PagedList<CategoryProductInfo> resultList = builderFactory.create(em, CategoryProductInfo.class)
                .selectNew(CategoryProductInfo.class)
                .with("c.name")
                .with("p.name")
                .with("p.price")
                .end()
                .from(Product.class, "p")
                .innerJoin("p.category", "c")
                .orderByDesc("p.id")
                .page(0, 10)
                .getResultList();

        resultList.stream().forEach(System.out::println);
    }

    public void findProductsAboveAveragePrice() {
        // method 1
//        List<Product> products = builderFactory.create(em, Product.class)
//                .from(Product.class, "p")
//                .whereSubquery()
//                .from(Product.class, "sub")
//                .select("avg(sub.price)")
//                .end()
//                .leExpression("p.price")
//                .getResultList();

        //method 2
        List<Product> products = builderFactory.create(em, Product.class)
                .from(Product.class, "p")
                .whereSubquery(
                        builderFactory.create(em, Double.class)
                                .from(Product.class, "sub")
                                .select("avg(sub.price)", "avgPrice")
                )
                .end()
                .leExpression("p.price")
                .getResultList();

        Util.printProduct(products);
    }


    public void countCategoriesWithMinProducts(int minCount) {
        List<Tuple> data = builderFactory.create(em, Tuple.class)
                .select("c.name", "categoryName")
                .select("count(p.id)", "productCount")
                .from(Product.class, "p")
                .innerJoin("p.category", "c")
                .groupBy("c.id")
                .having("count(p.id)").ge(minCount)
                .getResultList();

        data.forEach(tuple -> {
            String name = tuple.get("categoryName", String.class);
            Long count = tuple.get("productCount", Long.class);
            System.out.println("Category: " + name + ", Products: " + count);
        });
    }


    public void groupAndCountProductByCategory() {
        List<Tuple> resultList = builderFactory.create(em, Tuple.class)
                .from(Product.class, "p")
                .innerJoin("p.category", "c")
                .select("c.name", "categoryName")
                .select("count(p.id)", "productCount")
                .groupBy("c.id")
                .orderByDesc("productCount")
                .getResultList();

//        List<Tuple> resultList = builderFactory.create(em, Tuple.class)
//                .from(Product.class, "p")
//                .select("p.category.name", "categoryName")
//                .select("count(p.id)", "productCount")
//                .groupBy("p.category.id")
//                .orderByDesc("productCount")
//                .getResultList();

        resultList.forEach(tuple -> {
            String name = tuple.get("categoryName", String.class);
            Long count = tuple.get("productCount", Long.class);
            System.out.println("Category: " + name + ", Products: " + count);
        });
    }

    //lấy danh sách tất cả các Category chưa bị xóa (isDeleted = false), sắp xếp theo createdAt giảm dần.
    public void findAllActiveCategories(int page, int size) {
        PagedList<Category> resultList = builderFactory.create(em, Category.class)
                .where("isDeleted").eq(false)
                .orderByDesc("createdAt")
                .orderByAsc("id")
                .page(page * size, size)
                .getResultList();

        System.out.println("getFirstResult(): " + resultList.getFirstResult());
        System.out.println("getMaxResults(): " + resultList.getMaxResults());
        System.out.println("getPage(): " + resultList.getPage());
        System.out.println("getSize(): " + resultList.getSize());
        System.out.println("getTotalPages(): " + resultList.getTotalPages());
        System.out.println("getTotalSize(): " + resultList.getTotalSize());

        Util.print(resultList.stream().toList());

    }

    public void searchByNameCategory(String keyword) {
        List<Category> data = builderFactory.create(em, Category.class)
                .from(Category.class)
                .where("name").like(false).value("%" + keyword + "%").noEscape()
                .getResultList();
        Util.print(data);
    }

    public void findAndSortCategories() {
        List<Category> data = builderFactory.create(em, Category.class)
                .from(Category.class)
                .where("isDeleted").eq(false)
                .orderByAsc("createdAt")
                .getResultList();

        Util.print(data);
    }
}
