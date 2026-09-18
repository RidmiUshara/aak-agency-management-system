package lk.aak.agency.repository;

import lk.aak.agency.model.SupplierReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierReturnRepository extends JpaRepository<SupplierReturn, Long> {

    List<SupplierReturn> findAllByOrderByReturnDateDesc();
}
