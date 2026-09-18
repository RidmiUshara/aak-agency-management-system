package lk.aak.agency.service;

import lk.aak.agency.model.DeliveryTrip;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.SalesInvoiceItem;
import lk.aak.agency.repository.DeliveryTripRepository;
import lk.aak.agency.repository.EmployeeRepository;
import lk.aak.agency.repository.RouteRepository;
import lk.aak.agency.repository.SalesInvoiceItemRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.VehicleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeliveryTripService {

    private final DeliveryTripRepository deliveryTripRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceItemRepository salesInvoiceItemRepository;
    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final EmployeeRepository employeeRepository;

    public DeliveryTripService(
            DeliveryTripRepository deliveryTripRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            SalesInvoiceItemRepository salesInvoiceItemRepository,
            RouteRepository routeRepository,
            VehicleRepository vehicleRepository,
            EmployeeRepository employeeRepository) {

        this.deliveryTripRepository = deliveryTripRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesInvoiceItemRepository = salesInvoiceItemRepository;
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<DeliveryTrip> getAllTrips() {
        return deliveryTripRepository.findAll(Sort.by(Sort.Direction.DESC, "tripDate"));
    }

    public Optional<DeliveryTrip> getTripById(Long id) {
        return deliveryTripRepository.findById(id);
    }

    public DeliveryTrip saveTrip(DeliveryTrip trip) {

        routeRepository.findById(trip.getRouteId())
                .ifPresentOrElse(
                        route -> trip.setRouteName(route.getRouteName()),
                        () -> {
                            throw new IllegalArgumentException("Selected route was not found.");
                        }
                );

        vehicleRepository.findById(trip.getVehicleId())
                .ifPresentOrElse(
                        vehicle -> trip.setVehicleNumber(vehicle.getVehicleNumber()),
                        () -> {
                            throw new IllegalArgumentException("Selected vehicle was not found.");
                        }
                );

        if (trip.getDriverEmployeeId() != null) {
            trip.setDriverName(
                    employeeRepository.findById(trip.getDriverEmployeeId())
                            .map(employee -> employee.getFullName())
                            .orElse(null)
            );
        } else {
            trip.setDriverName(null);
        }

        if (trip.getHelperEmployeeId() != null) {
            trip.setHelperName(
                    employeeRepository.findById(trip.getHelperEmployeeId())
                            .map(employee -> employee.getFullName())
                            .orElse(null)
            );
        } else {
            trip.setHelperName(null);
        }

        return deliveryTripRepository.save(trip);
    }

    public List<SalesInvoice> getUnassignedCompletedInvoices() {
        return salesInvoiceRepository.findByStatusAndDeliveryTripIdIsNullOrderByInvoiceDateAsc("COMPLETED");
    }

    public List<SalesInvoice> getInvoicesForTrip(Long tripId) {
        return salesInvoiceRepository.findByDeliveryTripIdOrderByIdAsc(tripId);
    }

    @Transactional
    public void assignInvoiceToTrip(Long tripId, Long invoiceId) {

        DeliveryTrip trip = deliveryTripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery trip was not found."));

        SalesInvoice invoice = salesInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Sales invoice was not found."));

        if (!"COMPLETED".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException("Only completed bills can be assigned to a delivery trip.");
        }

        if (invoice.getDeliveryTripId() != null) {
            throw new IllegalArgumentException("This bill is already assigned to a delivery trip.");
        }

        invoice.setDeliveryTripId(trip.getId());
        invoice.setDeliveryStatus("PENDING");
        salesInvoiceRepository.save(invoice);
    }

    @Transactional
    public void removeInvoiceFromTrip(Long tripId, Long invoiceId) {

        SalesInvoice invoice = salesInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Sales invoice was not found."));

        if (!tripId.equals(invoice.getDeliveryTripId())) {
            throw new IllegalArgumentException("This bill is not assigned to the selected trip.");
        }

        invoice.setDeliveryTripId(null);
        invoice.setDeliveryStatus(null);
        salesInvoiceRepository.save(invoice);
    }

    @Transactional
    public void updateDeliveryStatus(Long tripId, Long invoiceId, String deliveryStatus) {

        SalesInvoice invoice = salesInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Sales invoice was not found."));

        if (!tripId.equals(invoice.getDeliveryTripId())) {
            throw new IllegalArgumentException("This bill is not assigned to the selected trip.");
        }

        invoice.setDeliveryStatus(deliveryStatus);
        salesInvoiceRepository.save(invoice);
    }

    /**
     * Combined planned product quantities across every bill assigned to this trip -
     * used as the loading summary the warehouse checks off against at morning loading.
     * Read-only for now; actual issued quantities and the warehouse-to-vehicle stock
     * transfer are handled in a follow-up increment.
     */
    public List<LoadingSummaryRow> getLoadingSummary(Long tripId) {

        Map<Long, LoadingSummaryRow> summaryByProduct = new LinkedHashMap<>();

        for (SalesInvoice invoice : getInvoicesForTrip(tripId)) {
            List<SalesInvoiceItem> items = salesInvoiceItemRepository
                    .findBySalesInvoiceIdOrderByIdAsc(invoice.getId());

            for (SalesInvoiceItem item : items) {
                Long productId = item.getProduct().getId();

                LoadingSummaryRow existingRow = summaryByProduct.get(productId);

                if (existingRow == null) {
                    summaryByProduct.put(
                            productId,
                            new LoadingSummaryRow(
                                    productId,
                                    item.getProduct().getProductName(),
                                    item.getSalesUnit(),
                                    item.getQuantity()
                            )
                    );
                } else {
                    existingRow.plannedQuantity = existingRow.plannedQuantity.add(item.getQuantity());
                }
            }
        }

        return List.copyOf(summaryByProduct.values());
    }

    public static class LoadingSummaryRow {

        private final Long productId;
        private final String productName;
        private final String unit;
        private BigDecimal plannedQuantity;

        public LoadingSummaryRow(Long productId, String productName, String unit, BigDecimal plannedQuantity) {
            this.productId = productId;
            this.productName = productName;
            this.unit = unit;
            this.plannedQuantity = plannedQuantity;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getUnit() {
            return unit;
        }

        public BigDecimal getPlannedQuantity() {
            return plannedQuantity;
        }
    }
}
