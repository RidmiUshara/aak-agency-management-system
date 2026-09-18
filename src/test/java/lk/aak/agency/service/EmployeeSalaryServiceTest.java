package lk.aak.agency.service;

import lk.aak.agency.model.Employee;
import lk.aak.agency.model.EmployeeAdvance;
import lk.aak.agency.model.EmployeeSalaryPayment;
import lk.aak.agency.repository.EmployeeAdvanceRepository;
import lk.aak.agency.repository.EmployeeRepository;
import lk.aak.agency.repository.EmployeeSalaryPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeSalaryServiceTest {

    @Mock
    private EmployeeSalaryPaymentRepository employeeSalaryPaymentRepository;
    @Mock
    private EmployeeAdvanceRepository employeeAdvanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeSalaryService newService() {
        return new EmployeeSalaryService(
                employeeSalaryPaymentRepository, employeeAdvanceRepository, employeeRepository
        );
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFullName("Nimal Rep");
        return employee;
    }

    @Test
    void recordSalaryPayment_deductsOnlySelectedAdvancesAndMarksThemSettled() {

        EmployeeSalaryService service = newService();

        EmployeeAdvance advance1 = new EmployeeAdvance();
        advance1.setId(10L);
        advance1.setAmount(new BigDecimal("500"));

        EmployeeAdvance advance2 = new EmployeeAdvance();
        advance2.setId(11L);
        advance2.setAmount(new BigDecimal("300"));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));
        when(employeeAdvanceRepository.findByEmployeeIdAndSettledFalseOrderByAdvanceDateAsc(1L))
                .thenReturn(List.of(advance1, advance2));
        when(employeeSalaryPaymentRepository.save(any(EmployeeSalaryPayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeSalaryPayment payment = service.recordSalaryPayment(
                1L, "2026-09", new BigDecimal("30000"), LocalDate.now(), null, List.of(10L)
        );

        assertThat(payment.getAdvanceDeduction()).isEqualByComparingTo("500");
        assertThat(payment.getNetPaid()).isEqualByComparingTo("29500");

        verify(employeeAdvanceRepository, times(1)).save(advance1);
        verify(employeeAdvanceRepository, never()).save(advance2);
        assertThat(advance1.isSettled()).isTrue();
        assertThat(advance2.isSettled()).isFalse();
    }

    @Test
    void recordSalaryPayment_rejectsDeductionGreaterThanGrossSalary() {

        EmployeeSalaryService service = newService();

        EmployeeAdvance advance = new EmployeeAdvance();
        advance.setId(10L);
        advance.setAmount(new BigDecimal("50000"));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee()));
        when(employeeAdvanceRepository.findByEmployeeIdAndSettledFalseOrderByAdvanceDateAsc(1L))
                .thenReturn(List.of(advance));

        assertThatThrownBy(() -> service.recordSalaryPayment(
                1L, "2026-09", new BigDecimal("30000"), LocalDate.now(), null, List.of(10L)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
