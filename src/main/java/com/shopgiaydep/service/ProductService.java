package com.shopgiaydep.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopgiaydep.dto.ColorVariantDTO;
import com.shopgiaydep.dto.ProductDTO;
import com.shopgiaydep.dto.ProductRequest;
import com.shopgiaydep.entity.ColorVariant;
import com.shopgiaydep.entity.Product;
import com.shopgiaydep.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    // Lấy danh sách sản phẩm có filter + phân trang
    public Page<ProductDTO> getProducts(String category, String search, Boolean isNew,
                                         Boolean isSale, Long minPrice, Long maxPrice,
                                         int page, int size, String sortBy) {
        Sort sort = switch (sortBy) {
            case "price-asc"  -> Sort.by("price").ascending();
            case "price-desc" -> Sort.by("price").descending();
            case "popular"    -> Sort.by("reviewCount").descending();
            default           -> Sort.by("createdAt").descending(); // newest
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository
                .findWithFilters(category, search, isNew, isSale, minPrice, maxPrice, pageable)
                .map(this::toDTO);
    }

    public ProductDTO getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id=" + id));
        return toDTO(product);
    }

    public ProductDTO getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + slug));
        return toDTO(product);
    }

    @Transactional
    public ProductDTO create(ProductRequest request) {
        String slug = request.getSlug() != null ? request.getSlug() : generateSlug(request.getName());

        // Đảm bảo slug unique
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .brand(request.getBrand())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .category(request.getCategory())
                .stock(request.getStock())
                .description(request.getDescription())
                .isNew(request.getIsNew())
                .isSale(request.getIsSale())
                .sizes(toJson(request.getSizes()))
                .build();

        Product saved = productRepository.save(product);

        // Thêm color variants
        if (request.getColorVariants() != null) {
            List<ColorVariant> variants = request.getColorVariants().stream()
                    .map(dto -> ColorVariant.builder()
                            .color(dto.getColor())
                            .imageUrl(dto.getImageUrl())
                            .product(saved)
                            .build())
                    .collect(Collectors.toList());
            saved.getColorVariants().addAll(variants);
            productRepository.save(saved);
        }

        return toDTO(saved);
    }

    @Transactional
    public ProductDTO update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id=" + id));

        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setIsNew(request.getIsNew());
        product.setIsSale(request.getIsSale());
        product.setSizes(toJson(request.getSizes()));

        // Cập nhật color variants: xóa cũ, thêm mới
        product.getColorVariants().clear();
        if (request.getColorVariants() != null) {
            request.getColorVariants().forEach(dto -> {
                ColorVariant cv = ColorVariant.builder()
                        .color(dto.getColor())
                        .imageUrl(dto.getImageUrl())
                        .product(product)
                        .build();
                product.getColorVariants().add(cv);
            });
        }

        return toDTO(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm id=" + id);
        }
        productRepository.deleteById(id);
    }

    // ─── Helpers ────────────────────────────────────────────────

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setBrand(p.getBrand());
        dto.setPrice(p.getPrice());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setCategory(p.getCategory());
        dto.setStock(p.getStock());
        dto.setDescription(p.getDescription());
        dto.setIsNew(p.getIsNew());
        dto.setIsSale(p.getIsSale());
        dto.setRating(p.getRating());
        dto.setReviewCount(p.getReviewCount());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setSizes(fromJson(p.getSizes()));
        dto.setColorVariants(p.getColorVariants().stream().map(cv -> {
            ColorVariantDTO cvDto = new ColorVariantDTO();
            cvDto.setId(cv.getId());
            cvDto.setColor(cv.getColor());
            cvDto.setImageUrl(cv.getImageUrl());
            return cvDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("")
                .toLowerCase()
                .replaceAll("[đĐ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        return slug;
    }

    private String toJson(List<String> list) {
        if (list == null) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
