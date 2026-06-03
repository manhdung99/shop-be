package com.shopgiaydep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "color_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColorVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;
}
