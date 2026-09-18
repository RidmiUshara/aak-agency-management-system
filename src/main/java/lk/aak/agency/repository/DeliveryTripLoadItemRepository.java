package lk.aak.agency.repository;

import lk.aak.agency.model.DeliveryTripLoadItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryTripLoadItemRepository extends JpaRepository<DeliveryTripLoadItem, Long> {

    List<DeliveryTripLoadItem> findByDeliveryTripIdOrderByIdAsc(Long deliveryTripId);

    boolean existsByDeliveryTripId(Long deliveryTripId);
}
