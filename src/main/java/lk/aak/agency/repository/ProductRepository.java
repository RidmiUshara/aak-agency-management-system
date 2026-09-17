package lk.aak.agency.repository;

import lk.aak.agency.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Optional<Product> findByCblProductCode(
            String cblProductCode
    );

    boolean existsByCblProductCode(
            String cblProductCode
    );

    List<Product> findByProductNameContainingIgnoreCase(
            String productName
    );
}