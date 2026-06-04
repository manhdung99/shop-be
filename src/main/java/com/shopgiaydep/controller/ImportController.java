package com.shopgiaydep.controller;

import com.shopgiaydep.dto.ApiResponse;
import com.shopgiaydep.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    /**
     * POST /api/import/products   (Admin only)
     * Body: multipart/form-data, field name = "file"
     */
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importProducts(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chỉ hỗ trợ file Excel (.xlsx, .xls)"));
        }

        try {
            Map<String, Object> result = importService.importFromExcel(file);
            int imported = (int) result.get("imported");
            int failed   = (int) result.get("failed");
            String msg   = String.format("Import xong: %d thành công, %d lỗi", imported, failed);
            return ResponseEntity.ok(ApiResponse.success(msg, result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi đọc file: " + e.getMessage()));
        }
    }
}
