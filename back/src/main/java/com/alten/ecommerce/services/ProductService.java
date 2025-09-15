package com.alten.ecommerce.services;

import com.alten.ecommerce.models.InventoryStatus;
import com.alten.ecommerce.models.dtos.ProductDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductService {

    @Transactional(readOnly = true)
    List<ProductDTO> getAllProducts();

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    void deleteProduct(Long id);

    @Transactional(readOnly = true)
    List<ProductDTO> getProductsByCategory(String category);

    @Transactional(readOnly = true)
    ProductDTO getProductById(Long id);

}
