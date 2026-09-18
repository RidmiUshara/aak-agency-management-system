package lk.aak.agency.repository;

import lk.aak.agency.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCode(String customerCode);

    boolean existsByCustomerCode(String customerCode);

    Optional<Customer> findTopByOrderByIdDesc();

    Optional<Customer> findByQrCode(String qrCode);
}