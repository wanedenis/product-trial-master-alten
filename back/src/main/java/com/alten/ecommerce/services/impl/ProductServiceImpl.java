package com.alten.ecommerce.services.impl;

import com.alten.ecommerce.models.Category;
import com.alten.ecommerce.models.InventoryStatus;
import com.alten.ecommerce.models.Product;
import com.alten.ecommerce.models.dtos.ProductDTO;
import com.alten.ecommerce.repositories.ProductRepository;
import com.alten.ecommerce.repositories.CategoryRepository;
import com.alten.ecommerce.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("authentication.principal.username == 'admin@admin.com'")
    public ProductDTO createProduct(ProductDTO productDTO) {

        // Check if product code already exists
        if (productRepository.existsByCode(productDTO.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("Product with code %s already exists", productDTO.code())
            );
        }

        // Find category by name
        Category category = categoryRepository.findByNameIgnoreCase(productDTO.category())
                .orElse(categoryRepository
                        .save(new Category(productDTO.category())));

        Product product = new Product();
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        getProductFromDTO(product, productDTO, category);

        Product saved = productRepository.save(product);

        return ProductDTO.fromEntity(saved);
    }

    @PreAuthorize("authentication.principal.username == 'admin@admin.com'")
    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Product with ID %d not found", id)
                ));

        // Check for code uniqueness (if code is being changed)
        if (!existingProduct.getCode().equals(productDTO.code()) &&
                productRepository.existsByCode(productDTO.code())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    String.format("Product with code %s already exists", productDTO.code())
            );
        }

        // Find category by name
        Category category = categoryRepository.findByNameIgnoreCase(productDTO.category())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Category with name %s not found", productDTO.category())
                ));

        getProductFromDTO(existingProduct, productDTO, category);
        existingProduct.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(existingProduct);
        return ProductDTO.fromEntity(updatedProduct);
    }

    private static void getProductFromDTO(Product product, ProductDTO productDTO, Category category) {
        product.setCode(productDTO.code());
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setImage(productDTO.image());
        product.setCategory(category);
        product.setPrice(productDTO.price());
        product.setQuantity(productDTO.quantity());
        product.setInternalReference(productDTO.internalReference());
        product.setShellId(productDTO.shellId());
        product.setInventoryStatus(productDTO.inventoryStatus());
        product.setRating(productDTO.rating());
    }

    @PreAuthorize("authentication.principal.username == 'admin@admin.com'")
    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Product with ID %d not found", id)
            );
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> getProductsByCategory(String category) {
        // Find category by name
        categoryRepository.findByNameIgnoreCase(category)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format("Category with name %s not found", category)
                ));

        return productRepository.findByCategory_Name(category)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public ProductDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Product with ID %d not found", id)
                ));
        return ProductDTO.fromEntity(product);
    }

}
