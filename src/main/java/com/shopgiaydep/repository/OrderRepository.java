package com.shopgiaydep.repository;

import com.shopgiaydep.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status = :status)
          AND (:search IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Order> findWithFilters(
            @Param("status") Order.OrderStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    long countByStatus(Order.OrderStatus status);
}
