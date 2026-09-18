package lk.aak.agency.service;

import lk.aak.agency.model.Employee;
import lk.aak.agency.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"));
    }

    public List<Employee> getActiveDrivers() {
        return employeeRepository.findByDesignationAndStatus("DRIVER", "ACTIVE");
    }

    public List<Employee> getActiveHelpers() {
        return employeeRepository.findByDesignationAndStatus("HELPER", "ACTIVE");
    }

    public List<Employee> getActiveFieldCollectors() {
        List<Employee> fieldCollectors = new ArrayList<>(
                employeeRepository.findByDesignationAndStatus("SALES_REP", "ACTIVE")
        );
        fieldCollectors.addAll(getActiveDrivers());
        return fieldCollectors;
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee saveEmployee(Employee employee) {
        if (employee.getId() == null
                && (employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank())) {

            employee.setEmployeeCode(generateEmployeeCode());
        }

        return employeeRepository.save(employee);
    }

    private String generateEmployeeCode() {
        long nextNumber = employeeRepository
                .findTopByOrderByIdDesc()
                .map(employee -> employee.getId() + 1)
                .orElse(1L);

        return String.format("EMP-%04d", nextNumber);
    }
}
