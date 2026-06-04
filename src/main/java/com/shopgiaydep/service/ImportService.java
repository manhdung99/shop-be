package com.shopgiaydep.service;

import com.shopgiaydep.entity.ColorVariant;
import com.shopgiaydep.entity.Product;
import com.shopgiaydep.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    /**
     * Đọc file Excel và import sản phẩm vào DB.
     * Format mỗi dòng:
     * name | brand | category | price | originalPrice | stock | description | isNew | isSale | sizes | color1 | imageUrl1 | color2 | imageUrl2 | ...
     */
    @Transactional
    public Map<String, Object> importFromExcel(MultipartFile file) throws Exception {
        List<String> success = new ArrayList<>();
        List<String> errors  = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            // Bỏ qua dòng header (row 0)
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    Product product = parseRow(row, i);
                    productRepository.save(product);
                    success.add("Dòng " + (i + 1) + ": " + product.getName());
                } catch (Exception e) {
                    log.error("Lỗi dòng {}: {}", i + 1, e.getMessage());
                    errors.add("Dòng " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        return Map.of(
            "imported", success.size(),
            "failed",   errors.size(),
            "success",  success,
            "errors",   errors
        );
    }

    private Product parseRow(Row row, int rowIndex) {
        String name          = getString(row, 0);
        String brand         = getString(row, 1);
        String category      = getString(row, 2);
        long   price         = getLong(row, 3);
        Long   originalPrice = row.getCell(4) != null && !getCellString(row.getCell(4)).isBlank()
                               ? getLong(row, 4) : null;
        int    stock         = (int) getLong(row, 5);
        String description   = getString(row, 6);
        boolean isNew        = getBoolean(row, 7);
        boolean isSale       = getBoolean(row, 8);
        String  sizesStr     = getString(row, 9);

        if (name.isBlank()) throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        if (brand.isBlank()) throw new IllegalArgumentException("Thương hiệu không được để trống");
        if (price <= 0)      throw new IllegalArgumentException("Giá không hợp lệ");

        // Tự generate slug
        String slug = generateSlug(name);
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        // Parse sizes: "38,39,40,41"
        String sizesJson = "[]";
        if (!sizesStr.isBlank()) {
            String[] parts = sizesStr.split("[,;]");
            StringJoiner sj = new StringJoiner("\",\"", "[\"", "\"]");
            for (String s : parts) {
                String trimmed = s.trim();
                if (!trimmed.isBlank()) sj.add(trimmed);
            }
            sizesJson = sj.toString();
        }

        Product product = Product.builder()
                .name(name)
                .slug(slug)
                .brand(brand)
                .category(category.isBlank() ? "unisex" : category)
                .price(price)
                .originalPrice(originalPrice)
                .stock(stock)
                .description(description)
                .isNew(isNew)
                .isSale(isSale)
                .sizes(sizesJson)
                .rating(0.0)
                .reviewCount(0)
                .build();

        // Parse color variants từ cột 10 trở đi: color | imageUrl | color | imageUrl ...
        List<ColorVariant> variants = new ArrayList<>();
        int col = 10;
        while (col + 1 < row.getLastCellNum()) {
            String color    = getString(row, col);
            String imageUrl = getString(row, col + 1);
            if (!color.isBlank()) {
                variants.add(ColorVariant.builder()
                        .color(color)
                        .imageUrl(imageUrl.isBlank()
                                ? "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80"
                                : imageUrl)
                        .product(product)
                        .build());
            }
            col += 2;
        }

        // Nếu không có variant nào, tạo 1 variant mặc định
        if (variants.isEmpty()) {
            variants.add(ColorVariant.builder()
                    .color("Mặc định")
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80")
                    .product(product)
                    .build());
        }

        product.getColorVariants().addAll(variants);
        return product;
    }

    // ─── Cell helpers ─────────────────────────────────────────────────

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell == null ? "" : getCellString(cell).trim();
    }

    private String getCellString(Cell cell) {
        CellType type = cell.getCellType();

        // Với FORMULA cell: lấy kiểu kết quả thực tế
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        return switch (type) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private long getLong(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING  -> {
                try { yield Long.parseLong(cell.getStringCellValue().replaceAll("[^0-9]", "")); }
                catch (NumberFormatException e) { yield 0L; }
            }
            default -> 0L;
        };
    }

    private boolean getBoolean(Row row, int col) {
        String val = getString(row, col).toLowerCase();
        return val.equals("true") || val.equals("1") || val.equals("yes") || val.equals("có");
    }

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c <= 5; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
