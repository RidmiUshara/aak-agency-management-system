package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public ProductService(
            ProductRepository productRepository,
            AuditLogService auditLogService) {

        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    public List<Product> getAllProducts() {

        return productRepository.findAll(
                Sort.by(
                        Sort.Direction.ASC,
                        "productName"
                )
        );
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByCblCode(
            String cblProductCode) {

        return productRepository.findByCblProductCode(
                cblProductCode
        );
    }

    public List<Product> searchProducts(
            String productName) {

        if (productName == null ||
                productName.isBlank()) {

            return getAllProducts();
        }

        return productRepository
                .findByProductNameContainingIgnoreCase(
                        productName.trim()
                );
    }

    public Product saveProduct(Product product) {

        String cblCode = product
                .getCblProductCode()
                .trim()
                .toUpperCase();

        product.setCblProductCode(cblCode);

        Optional<Product> existingProduct =
                productRepository
                        .findByCblProductCode(cblCode);

        if (existingProduct.isPresent() &&
                !existingProduct
                        .get()
                        .getId()
                        .equals(product.getId())) {

            throw new IllegalArgumentException(
                    "This CBL product code already exists."
            );
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id).orElse(null);

        productRepository.deleteById(id);

        auditLogService.record(
                "PRODUCT_DELETED", "Product", id,
                product == null ? null : "Deleted product \"" + product.getProductName() + "\""
        );
    }
}