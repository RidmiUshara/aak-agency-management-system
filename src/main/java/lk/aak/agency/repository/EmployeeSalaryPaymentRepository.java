package lk.aak.agency.repository;

import lk.aak.agency.model.EmployeeSalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeSalaryPaymentRepository extends JpaRepository<EmployeeSalaryPayment, Long> {

    List<EmployeeSalaryPayment> findAllByOrderByPaymentDateDesc();

    List<EmployeeSalaryPayment> findByEmployeeIdOrderByPaymentDateDesc(Long employeeId);
}
