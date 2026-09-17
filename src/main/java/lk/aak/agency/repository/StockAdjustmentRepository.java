package lk.aak.agency.repository;

import lk.aak.agency.model.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockAdjustmentRepository
        extends JpaRepository<StockAdjustment, Long> {

    List<StockAdjustment>
    findAllByOrderByAdjustmentDateDesc();

    List<StockAdjustment>
    findByProductIdOrderByAdjustmentDateDesc(
            Long productId
    );
}