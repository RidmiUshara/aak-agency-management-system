package lk.aak.agency.repository;

import lk.aak.agency.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findTopByOrderByIdDesc();

    java.util.List<Employee> findByDesignationAndStatus(String designation, String status);
}
