package lk.aak.agency.repository;

import lk.aak.agency.model.DeliveryTrip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryTripRepository extends JpaRepository<DeliveryTrip, Long> {

    List<DeliveryTrip> findByStatusOrderByTripDateAsc(String status);
}
