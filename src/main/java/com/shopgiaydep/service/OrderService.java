package com.shopgiaydep.service;

import com.shopgiaydep.dto.OrderDTO;
import com.shopgiaydep.dto.OrderRequest;
import com.shopgiaydep.entity.Order;
import com.shopgiaydep.entity.OrderItem;
import com.shopgiaydep.entity.Product;
import com.shopgiaydep.repository.OrderRepository;
import com.shopgiaydep.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public Page<OrderDTO> getOrders(String statusStr, String search, int page, int size) {
        Order.OrderStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown status filter: {}", statusStr);
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findWithFilters(status, search, pageable).map(this::toDTO);
    }

    public OrderDTO getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng id=" + id));
        return toDTO(order);
    }

    @Transactional
    public OrderDTO create(OrderRequest request) {
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .address(request.getAddress())
                .note(request.getNote())
                .shippingMethod(request.getShippingMethod())
                .paymentMethod(request.getPaymentMethod())
                .status(Order.OrderStatus.PENDING)
                .total(0L)
                .build();

        Order saved = orderRepository.save(order);

        long total = 0L;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id=" + itemReq.getProductId()));

            OrderItem item = OrderItem.builder()
                    .order(saved)
                    .product(product)
                    .size(itemReq.getSize())
                    .color(itemReq.getColor())
                    .quantity(itemReq.getQuantity())
                    .price(product.getPrice())
                    .build();

            saved.getItems().add(item);
            total += product.getPrice() * itemReq.getQuantity();
        }

        // Tính phí ship
        long shipping = total >= 500000 ? 0 : 30000;
        saved.setTotal(total + shipping);

        return toDTO(orderRepository.save(saved));
    }

    @Transactional
    public OrderDTO updateStatus(Long id, String statusStr) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng id=" + id));

        Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
        order.setStatus(status);
        return toDTO(orderRepository.save(order));
    }

    // Stats cho dashboard
    public long countByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public long countAll() {
        return orderRepository.count();
    }

    // ─── Helper ────────────────────────────────────────────────

    private OrderDTO toDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setId(o.getId());
        dto.setCustomerName(o.getCustomerName());
        dto.setCustomerEmail(o.getCustomerEmail());
        dto.setCustomerPhone(o.getCustomerPhone());
        dto.setAddress(o.getAddress());
        dto.setNote(o.getNote());
        dto.setStatus(o.getStatus());
        dto.setTotal(o.getTotal());
        dto.setShippingMethod(o.getShippingMethod());
        dto.setPaymentMethod(o.getPaymentMethod());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setItems(o.getItems().stream().map(item -> {
            OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProduct().getName());
            // Lấy ảnh màu đầu tiên nếu có, fallback về colorVariants[0]
            itemDTO.setProductImage(
                item.getProduct().getColorVariants().isEmpty()
                    ? ""
                    : item.getProduct().getColorVariants().get(0).getImageUrl()
            );
            itemDTO.setSize(item.getSize());
            itemDTO.setColor(item.getColor());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            return itemDTO;
        }).collect(Collectors.toList()));
        return dto;
    }
}
