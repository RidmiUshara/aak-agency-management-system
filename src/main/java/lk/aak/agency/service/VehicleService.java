package lk.aak.agency.service;

import lk.aak.agency.model.Vehicle;
import lk.aak.agency.repository.VehicleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final EmployeeService employeeService;

    public VehicleService(VehicleRepository vehicleRepository, EmployeeService employeeService) {
        this.vehicleRepository = vehicleRepository;
        this.employeeService = employeeService;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll(Sort.by(Sort.Direction.ASC, "vehicleNumber"));
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        String vehicleNumber = vehicle.getVehicleNumber().trim().toUpperCase();

        Optional<Vehicle> existing = vehicleRepository.findAll().stream()
                .filter(v -> v.getVehicleNumber().equalsIgnoreCase(vehicleNumber))
                .findFirst();

        if (existing.isPresent() && !existing.get().getId().equals(vehicle.getId())) {
            throw new IllegalArgumentException(
                    "A vehicle with the number \"" + vehicleNumber + "\" already exists."
            );
        }

        vehicle.setVehicleNumber(vehicleNumber);

        if (vehicle.getAssignedDriverId() != null) {
            vehicle.setAssignedDriverName(
                    employeeService.getEmployeeById(vehicle.getAssignedDriverId())
                            .map(employee -> employee.getFullName())
                            .orElse(null)
            );
        } else {
            vehicle.setAssignedDriverName(null);
        }

        return vehicleRepository.save(vehicle);
    }
}
