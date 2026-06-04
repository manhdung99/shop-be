package com.shopgiaydep.controller;

import com.shopgiaydep.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    /**
     * POST /api/upload/image   (Admin only)
     * Nhận file ảnh, lưu vào thư mục uploads/, trả về URL public
     */
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File trống"));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File quá lớn (tối đa 5MB)"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ hỗ trợ JPG, PNG, WEBP, GIF"));
        }

        try {
            // Tạo thư mục uploads nếu chưa có
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique để tránh trùng
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + ext;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = baseUrl + "/uploads/" + filename;
            log.info("Uploaded image: {}", url);

            return ResponseEntity.ok(ApiResponse.success("Upload thành công", Map.of("url", url)));

        } catch (IOException e) {
            log.error("Upload failed", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi lưu file: " + e.getMessage()));
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
