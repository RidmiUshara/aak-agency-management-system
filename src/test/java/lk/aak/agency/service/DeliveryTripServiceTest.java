package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.SalesInvoiceItem;
import lk.aak.agency.repository.DeliveryTripRepository;
import lk.aak.agency.repository.EmployeeRepository;
import lk.aak.agency.repository.RouteRepository;
import lk.aak.agency.repository.SalesInvoiceItemRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryTripServiceTest {

    @Mock
    private DeliveryTripRepository deliveryTripRepository;
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
                deliveryTripRepository, salesInvoiceRepository, salesInvoiceItemRepository,
                routeRepository, vehicleRepository, employeeRepository
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
}
