package lk.aak.agency.repository;

import lk.aak.agency.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement>
    findByProductIdOrderByMovementDateDesc(Long productId);

    boolean existsByReferenceTypeAndReferenceItemId(
            String referenceType,
            Long referenceItemId
    );

    @Query("""
            SELECT COALESCE(SUM(stock.quantityChange), 0)
            FROM StockMovement stock
            WHERE stock.product.id = :productId
            """)
    BigDecimal calculateCurrentStock(
            @Param("productId") Long productId
    );
}