package com.shopgiaydep.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String slug;
    private String brand;
    private Long price;
    private Long originalPrice;
    private String category;
    private Integer stock;
    private String description;
    private Boolean isNew;
    private Boolean isSale;
    private Double rating;
    private Integer reviewCount;
    private List<String> sizes;
    private List<ColorVariantDTO> colorVariants;
    private LocalDateTime createdAt;
}
