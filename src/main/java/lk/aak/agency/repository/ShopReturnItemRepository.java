package lk.aak.agency.repository;

import lk.aak.agency.model.ShopReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopReturnItemRepository extends JpaRepository<ShopReturnItem, Long> {

    List<ShopReturnItem> findByShopReturnIdOrderByIdAsc(Long shopReturnId);
}
