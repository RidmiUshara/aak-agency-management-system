package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_salary_payments")
public class EmployeeSalaryPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    // e.g. "2026-09" - the month this salary payment covers.
    @Column(name = "pay_period_month", nullable = false, length = 7)
    private String payPeriodMonth;

    @Column(name = "gross_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "advance_deduction", nullable = false, precision = 15, scale = 2)
    private BigDecimal advanceDeduction;

    @Column(name = "net_paid", nullable = false, precision = 15, scale = 2)
    private BigDecimal netPaid;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EmployeeSalaryPayment() {
    }

    @PrePersist
    public void setDefaultValues() {

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        if (advanceDeduction == null) {
            advanceDeduction = BigDecimal.ZERO;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPayPeriodMonth() {
        return payPeriodMonth;
    }

    public void setPayPeriodMonth(String payPeriodMonth) {
        this.payPeriodMonth = payPeriodMonth;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getAdvanceDeduction() {
        return advanceDeduction;
    }

    public void setAdvanceDeduction(BigDecimal advanceDeduction) {
        this.advanceDeduction = advanceDeduction;
    }

    public BigDecimal getNetPaid() {
        return netPaid;
    }

    public void setNetPaid(BigDecimal netPaid) {
        this.netPaid = netPaid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
