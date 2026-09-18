package lk.aak.agency.repository;

import lk.aak.agency.model.SupplierReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierReturnItemRepository extends JpaRepository<SupplierReturnItem, Long> {

    List<SupplierReturnItem> findBySupplierReturnIdOrderByIdAsc(Long supplierReturnId);
}
