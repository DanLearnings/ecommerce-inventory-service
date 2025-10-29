package com.danlearnings.ecommerce.inventoryservice.service;

import com.danlearnings.ecommerce.inventoryservice.model.Product;
import com.danlearnings.ecommerce.inventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setQuantity(productDetails.getQuantity());
                    product.setSku(productDetails.getSku());
                    return productRepository.save(product);
                });
    }

    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return true;
                })
                .orElse(false);
    }

    public boolean checkStock(Long productId, Integer requiredQuantity) {
        return productRepository.findById(productId)
                .map(product -> product.getQuantity() >= requiredQuantity)
                .orElse(false);
    }

    public Optional<Product> decreaseStock(Long productId, Integer quantity) {
        return productRepository.findById(productId)
                .map(product -> {
                    if (product.getQuantity() >= quantity) {
                        product.setQuantity(product.getQuantity() - quantity);
                        return productRepository.save(product);
                    }
                    return null;
                });
    }
}