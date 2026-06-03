package com.shopgiaydep.controller;

import com.shopgiaydep.dto.ApiResponse;
import com.shopgiaydep.dto.OrderDTO;
import com.shopgiaydep.dto.OrderRequest;
import com.shopgiaydep.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders  (Public - khách hàng đặt hàng)
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> create(@Valid @RequestBody OrderRequest request) {
        OrderDTO order = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công", order));
    }

    // GET /api/orders  (Admin only)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrders(status, search, page, size)));
    }

    // GET /api/orders/{id}  (Admin only)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    // PUT /api/orders/{id}/status  (Admin only)
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công",
                orderService.updateStatus(id, status)));
    }

    // GET /api/orders/stats  (Admin only - cho Dashboard)
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = Map.of(
            "total", orderService.countAll(),
            "pending", orderService.countByStatus(com.shopgiaydep.entity.Order.OrderStatus.PENDING),
            "processing", orderService.countByStatus(com.shopgiaydep.entity.Order.OrderStatus.PROCESSING),
            "delivered", orderService.countByStatus(com.shopgiaydep.entity.Order.OrderStatus.DELIVERED)
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
