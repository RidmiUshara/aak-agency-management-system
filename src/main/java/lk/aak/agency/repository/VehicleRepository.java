package lk.aak.agency.repository;

import lk.aak.agency.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByVehicleNumber(String vehicleNumber);
}
