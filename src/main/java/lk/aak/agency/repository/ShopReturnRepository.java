package lk.aak.agency.repository;

import lk.aak.agency.model.ShopReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopReturnRepository extends JpaRepository<ShopReturn, Long> {

    List<ShopReturn> findAllByOrderByReturnDateDesc();

    List<ShopReturn> findByCustomerIdOrderByReturnDateDesc(Long customerId);
}
