package lk.aak.agency.service;

import lk.aak.agency.model.Employee;
import lk.aak.agency.model.EmployeeAdvance;
import lk.aak.agency.repository.EmployeeAdvanceRepository;
import lk.aak.agency.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeAdvanceService {

    private final EmployeeAdvanceRepository employeeAdvanceRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeAdvanceService(
            EmployeeAdvanceRepository employeeAdvanceRepository,
            EmployeeRepository employeeRepository) {

        this.employeeAdvanceRepository = employeeAdvanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeAdvance> getAllAdvances() {
        return employeeAdvanceRepository.findAllByOrderByAdvanceDateDesc();
    }

    public List<EmployeeAdvance> getAdvancesForEmployee(Long employeeId) {
        return employeeAdvanceRepository.findByEmployeeIdOrderByAdvanceDateDesc(employeeId);
    }

    public List<EmployeeAdvance> getUnsettledAdvances(Long employeeId) {
        return employeeAdvanceRepository.findByEmployeeIdAndSettledFalseOrderByAdvanceDateAsc(employeeId);
    }

    public BigDecimal getUnsettledTotal(Long employeeId) {
        return getUnsettledAdvances(employeeId).stream()
                .map(EmployeeAdvance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public EmployeeAdvance giveAdvance(Long employeeId, BigDecimal amount, LocalDate advanceDate, String notes) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Selected employee was not found."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advance amount must be greater than zero.");
        }

        EmployeeAdvance advance = new EmployeeAdvance();
        advance.setEmployeeId(employeeId);
        advance.setEmployeeName(employee.getFullName());
        advance.setAmount(amount);
        advance.setAdvanceDate(advanceDate != null ? advanceDate : LocalDate.now());
        advance.setNotes(notes);

        return employeeAdvanceRepository.save(advance);
    }
}
