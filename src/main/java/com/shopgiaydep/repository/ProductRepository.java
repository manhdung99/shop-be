package com.shopgiaydep.repository;

import com.shopgiaydep.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Tìm kiếm + filter
    @Query("""
        SELECT p FROM Product p
        WHERE (:category IS NULL OR p.category = :category)
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isNew IS NULL OR p.isNew = :isNew)
          AND (:isSale IS NULL OR p.isSale = :isSale)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<Product> findWithFilters(
            @Param("category") String category,
            @Param("search") String search,
            @Param("isNew") Boolean isNew,
            @Param("isSale") Boolean isSale,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            Pageable pageable
    );
}
