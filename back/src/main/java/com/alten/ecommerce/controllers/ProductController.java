package com.alten.ecommerce.controllers;

import com.alten.ecommerce.models.dtos.ProductDTO;
import com.alten.ecommerce.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Slf4j
@Tag(name = "Products", description = "Product management operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private final ProductService productServiceImpl;

    @Autowired
    public ProductController(ProductService productService) {
        this.productServiceImpl = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves a list of all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of products"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<ProductDTO> getAllProducts() {
        return productServiceImpl.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ProductDTO getProductById(@PathVariable Long id) {
        return productServiceImpl.getProductById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Requires admin access"),
            @ApiResponse(responseCode = "409", description = "Product code already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ProductDTO createProduct(@Valid @RequestBody ProductDTO productDTO) {
        return productServiceImpl.createProduct(productDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Updates an existing product by ID (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Requires admin access"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product code already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ProductDTO updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        return productServiceImpl.updateProduct(id, productDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Deletes a product by ID (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Requires admin access"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productServiceImpl.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryName}")
    @Operation(summary = "Get products by category", description = "Retrieves products by category name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<ProductDTO> getProductsByCategory(@PathVariable String categoryName) {
        return productServiceImpl.getProductsByCategory(categoryName);
    }

}
