package lk.aak.agency.service;

import lk.aak.agency.model.DeliveryTrip;
import lk.aak.agency.model.DeliveryTripLoadItem;
import lk.aak.agency.model.Product;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.SalesInvoiceItem;
import lk.aak.agency.repository.DeliveryTripLoadItemRepository;
import lk.aak.agency.repository.DeliveryTripRepository;
import lk.aak.agency.repository.EmployeeRepository;
import lk.aak.agency.repository.RouteRepository;
import lk.aak.agency.repository.SalesInvoiceItemRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryTripServiceTest {

    @Mock
    private DeliveryTripRepository deliveryTripRepository;
    @Mock
    private DeliveryTripLoadItemRepository deliveryTripLoadItemRepository;
    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;
    @Mock
    private SalesInvoiceItemRepository salesInvoiceItemRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void getLoadingSummary_combinesQuantitiesOfTheSameProductAcrossBills() {
        DeliveryTripService service = new DeliveryTripService(
                deliveryTripRepository, deliveryTripLoadItemRepository, salesInvoiceRepository,
                salesInvoiceItemRepository, routeRepository, vehicleRepository, employeeRepository
        );

        SalesInvoice invoiceOne = new SalesInvoice();
        invoiceOne.setId(1L);
        SalesInvoice invoiceTwo = new SalesInvoice();
        invoiceTwo.setId(2L);

        when(salesInvoiceRepository.findByDeliveryTripIdOrderByIdAsc(10L))
                .thenReturn(List.of(invoiceOne, invoiceTwo));

        Product chocolate = new Product();
        chocolate.setId(100L);
        chocolate.setProductName("Chunky Choc Trio");

        SalesInvoiceItem item1 = new SalesInvoiceItem();
        item1.setProduct(chocolate);
        item1.setQuantity(new BigDecimal("5"));
        item1.setSalesUnit("PKT");

        SalesInvoiceItem item2 = new SalesInvoiceItem();
        item2.setProduct(chocolate);
        item2.setQuantity(new BigDecimal("3"));
        item2.setSalesUnit("PKT");

        when(salesInvoiceItemRepository.findBySalesInvoiceIdOrderByIdAsc(1L)).thenReturn(List.of(item1));
        when(salesInvoiceItemRepository.findBySalesInvoiceIdOrderByIdAsc(2L)).thenReturn(List.of(item2));

        List<DeliveryTripService.LoadingSummaryRow> summary = service.getLoadingSummary(10L);

        assertThat(summary).hasSize(1);
        assertThat(summary.get(0).getProductName()).isEqualTo("Chunky Choc Trio");
        assertThat(summary.get(0).getPlannedQuantity()).isEqualByComparingTo("8");
    }

    @Test
    void confirmLoading_createsLoadItemsFromPlannedSummaryAndMarksTripLoaded() {
        DeliveryTripService service = new DeliveryTripService(
                deliveryTripRepository, deliveryTripLoadItemRepository, salesInvoiceRepository,
                salesInvoiceItemRepository, routeRepository, vehicleRepository, employeeRepository
        );

        DeliveryTrip trip = new DeliveryTrip();
        trip.setId(10L);
        trip.setStatus("PLANNED");
        trip.setVehicleNumber("NA-1234");

        when(deliveryTripRepository.findById(10L)).thenReturn(Optional.of(trip));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        when(salesInvoiceRepository.findByDeliveryTripIdOrderByIdAsc(10L)).thenReturn(List.of(invoice));

        Product chocolate = new Product();
        chocolate.setId(100L);
        chocolate.setProductName("Chunky Choc Trio");

        SalesInvoiceItem item = new SalesInvoiceItem();
        item.setProduct(chocolate);
        item.setQuantity(new BigDecimal("5"));
        item.setSalesUnit("PKT");
        when(salesInvoiceItemRepository.findBySalesInvoiceIdOrderByIdAsc(1L)).thenReturn(List.of(item));

        service.confirmLoading(10L, Map.of(100L, new BigDecimal("4")));

        assertThat(trip.getStatus()).isEqualTo("LOADED");

        ArgumentCaptor<DeliveryTripLoadItem> loadItemCaptor = ArgumentCaptor.forClass(DeliveryTripLoadItem.class);
        verify(deliveryTripLoadItemRepository).save(loadItemCaptor.capture());

        DeliveryTripLoadItem savedLoadItem = loadItemCaptor.getValue();
        assertThat(savedLoadItem.getPlannedQuantity()).isEqualByComparingTo("5");
        assertThat(savedLoadItem.getLoadedQuantity()).isEqualByComparingTo("4");

        verify(deliveryTripRepository).save(trip);
    }

    @Test
    void confirmLoading_rejectsTripThatIsNotPlanned() {
        DeliveryTripService service = new DeliveryTripService(
                deliveryTripRepository, deliveryTripLoadItemRepository, salesInvoiceRepository,
                salesInvoiceItemRepository, routeRepository, vehicleRepository, employeeRepository
        );

        DeliveryTrip trip = new DeliveryTrip();
        trip.setId(10L);
        trip.setStatus("LOADED");

        when(deliveryTripRepository.findById(10L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> service.confirmLoading(10L, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(deliveryTripLoadItemRepository, never()).save(any());
    }

    @Test
    void getVehicleStockSummary_sumsLoadedQuantitiesOnlyForTripsStillLoaded() {
        DeliveryTripService service = new DeliveryTripService(
                deliveryTripRepository, deliveryTripLoadItemRepository, salesInvoiceRepository,
                salesInvoiceItemRepository, routeRepository, vehicleRepository, employeeRepository
        );

        DeliveryTrip loadedTrip = new DeliveryTrip();
        loadedTrip.setId(20L);
        loadedTrip.setVehicleNumber("NA-1234");
        loadedTrip.setStatus("LOADED");

        when(deliveryTripRepository.findByStatusOrderByTripDateAsc("LOADED"))
                .thenReturn(List.of(loadedTrip));

        DeliveryTripLoadItem loadItem1 = new DeliveryTripLoadItem();
        loadItem1.setProductId(100L);
        loadItem1.setProductName("Chunky Choc Trio");
        loadItem1.setUnit("PKT");
        loadItem1.setLoadedQuantity(new BigDecimal("4"));

        DeliveryTripLoadItem loadItem2 = new DeliveryTripLoadItem();
        loadItem2.setProductId(100L);
        loadItem2.setProductName("Chunky Choc Trio");
        loadItem2.setUnit("PKT");
        loadItem2.setLoadedQuantity(new BigDecimal("2"));

        when(deliveryTripLoadItemRepository.findByDeliveryTripIdOrderByIdAsc(20L))
                .thenReturn(List.of(loadItem1, loadItem2));

        List<DeliveryTripService.VehicleStockRow> stock = service.getVehicleStockSummary();

        assertThat(stock).hasSize(1);
        assertThat(stock.get(0).getVehicleNumber()).isEqualTo("NA-1234");
        assertThat(stock.get(0).getLoadedQuantity()).isEqualByComparingTo("6");
    }
}
