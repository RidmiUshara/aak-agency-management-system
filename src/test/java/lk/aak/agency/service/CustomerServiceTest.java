package lk.aak.agency.service;

import lk.aak.agency.model.Customer;
import lk.aak.agency.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void saveCustomer_generatesCustomerCode_whenNewCustomerHasNoCode() {
        Customer customer = new Customer();
        customer.setCustomerName("New Supermarket");

        when(customerRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer saved = customerService.saveCustomer(customer);

        assertThat(saved.getCustomerCode()).isEqualTo("AAK-000001");
        verify(customerRepository).save(customer);
    }

    @Test
    void saveCustomer_keepsExistingCode_whenEditingCustomer() {
        Customer customer = new Customer();
        customer.setId(5L);
        customer.setCustomerCode("AAK-000005");
        customer.setCustomerName("Existing Supermarket");

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer saved = customerService.saveCustomer(customer);

        assertThat(saved.getCustomerCode()).isEqualTo("AAK-000005");
    }
}
