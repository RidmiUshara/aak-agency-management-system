package lk.aak.agency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "delivery_trips")
public class DeliveryTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trip date is required.")
    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;

    @NotNull(message = "Route is required.")
    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "route_name")
    private String routeName;

    @NotNull(message = "Vehicle is required.")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "driver_employee_id")
    private Long driverEmployeeId;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "helper_employee_id")
    private Long helperEmployeeId;

    @Column(name = "helper_name")
    private String helperName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public DeliveryTrip() {
    }

    @PrePersist
    public void setDefaultValues() {
        if (status == null || status.isBlank()) {
            status = "PLANNED";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Long getDriverEmployeeId() {
        return driverEmployeeId;
    }

    public void setDriverEmployeeId(Long driverEmployeeId) {
        this.driverEmployeeId = driverEmployeeId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Long getHelperEmployeeId() {
        return helperEmployeeId;
    }

    public void setHelperEmployeeId(Long helperEmployeeId) {
        this.helperEmployeeId = helperEmployeeId;
    }

    public String getHelperName() {
        return helperName;
    }

    public void setHelperName(String helperName) {
        this.helperName = helperName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
