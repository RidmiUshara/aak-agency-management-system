package lk.aak.agency.service;

import lk.aak.agency.model.Customer;
import lk.aak.agency.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    public CustomerService(CustomerRepository customerRepository, AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll(
                Sort.by(Sort.Direction.ASC, "customerName")
        );
    }

    public Page<Customer> getCustomers(int page, int size) {
        return customerRepository.findAll(
                PageRequest.of(
                        Math.max(page, 0),
                        Math.max(size, 1),
                        Sort.by(Sort.Direction.ASC, "customerName")
                )
        );
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByQrCode(String qrCode) {
        return customerRepository.findByQrCode(qrCode);
    }

    public Customer saveCustomer(Customer customer) {

        if (customer.getId() == null &&
                (customer.getCustomerCode() == null ||
                        customer.getCustomerCode().isBlank())) {

            customer.setCustomerCode(generateCustomerCode());
        }

        if (customer.getId() == null &&
                (customer.getQrCode() == null || customer.getQrCode().isBlank())) {

            customer.setQrCode(UUID.randomUUID().toString());
        }

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {

        Customer customer = customerRepository.findById(id).orElse(null);

        customerRepository.deleteById(id);

        auditLogService.record(
                "CUSTOMER_DELETED", "Customer", id,
                customer == null ? null : "Deleted customer \"" + customer.getCustomerName() + "\""
        );
    }

    private String generateCustomerCode() {

        long nextNumber = customerRepository
                .findTopByOrderByIdDesc()
                .map(customer -> customer.getId() + 1)
                .orElse(1L);

        return String.format("AAK-%06d", nextNumber);
    }
}
