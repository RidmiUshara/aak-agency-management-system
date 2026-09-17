package lk.aak.agency.service;

import lk.aak.agency.model.Customer;
import lk.aak.agency.repository.CustomerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll(
                Sort.by(Sort.Direction.ASC, "customerName")
        );
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {

        if (customer.getId() == null &&
                (customer.getCustomerCode() == null ||
                        customer.getCustomerCode().isBlank())) {

            customer.setCustomerCode(generateCustomerCode());
        }

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    private String generateCustomerCode() {

        long nextNumber = customerRepository
                .findTopByOrderByIdDesc()
                .map(customer -> customer.getId() + 1)
                .orElse(1L);

        return String.format("AAK-%06d", nextNumber);
    }
}
