package lk.aak.agency.service;

import lk.aak.agency.model.Employee;
import lk.aak.agency.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void saveEmployee_generatesEmployeeCode_whenNewEmployeeHasNoCode() {
        Employee employee = new Employee();
        employee.setFullName("Nimal Perera");
        employee.setDesignation("DRIVER");

        when(employeeRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Employee saved = employeeService.saveEmployee(employee);

        assertThat(saved.getEmployeeCode()).isEqualTo("EMP-0001");
    }
}
