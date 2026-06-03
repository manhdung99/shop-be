package com.shopgiaydep.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String slug; // nếu null sẽ tự generate từ name

    @NotBlank(message = "Thương hiệu không được để trống")
    private String brand;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá phải lớn hơn 0")
    private Long price;

    private Long originalPrice;

    @NotBlank(message = "Danh mục không được để trống")
    private String category;

    @NotNull(message = "Tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stock;

    private String description;

    private Boolean isNew = false;
    private Boolean isSale = false;

    private List<String> sizes;

    private List<ColorVariantDTO> colorVariants;
}
