package com.alten.ecommerce.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Product code is mandatory")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Product name is mandatory")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Product description must not exceed 2000 characters")
    private String description;

    @Column(name = "image", length = 500)
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_category_product"))
    @NotBlank(message = "Product category is mandatory")
    private Category category;

    @Column(name = "price", nullable = false)
    @NotNull(message = "Product price is mandatory")
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "Product quantity is mandatory")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Column(name = "internal_reference", length = 100)
    @Size(max = 100, message = "Internal reference must not exceed 100 characters")
    private String internalReference;

    @Column(name = "shell_id")
    private Long shellId;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false, length = 20)
    @NotNull(message = "Inventory status is mandatory")
    private InventoryStatus inventoryStatus;

    @Column(name = "rating")
    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    @Digits(integer = 1, fraction = 2, message = "Rating format is invalid")
    private BigDecimal rating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
