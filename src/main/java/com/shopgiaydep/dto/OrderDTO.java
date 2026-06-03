package com.shopgiaydep.dto;

import com.shopgiaydep.entity.Order;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String address;
    private String note;
    private Order.OrderStatus status;
    private Long total;
    private String shippingMethod;
    private String paymentMethod;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;

    @Data
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private String size;
        private String color;
        private Integer quantity;
        private Long price;
    }
}
