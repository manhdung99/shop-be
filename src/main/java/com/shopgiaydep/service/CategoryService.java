package com.shopgiaydep.service;

import com.shopgiaydep.entity.Category;
import com.shopgiaydep.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục id=" + id));
    }

    public Category create(Category request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Slug đã tồn tại: " + request.getSlug());
        }
        return categoryRepository.save(request);
    }

    public Category update(Long id, Category request) {
        Category category = getById(id);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục id=" + id);
        }
        categoryRepository.deleteById(id);
    }
}
