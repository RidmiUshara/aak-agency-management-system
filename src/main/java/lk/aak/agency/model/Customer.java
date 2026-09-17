package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", nullable = false, unique = true)
    private String customerCode;

    @Column(name = "cbl_outlet_id")
    private String cblOutletId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_type")
    private String customerType;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String area;

    @Column(name = "contact_person")
    private String contactPerson;

    private String phone;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays;

    @Column(name = "assigned_employee")
    private String assignedEmployee;

    private String status;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Customer() {
    }

    @PrePersist
    public void setDefaultValues() {
        if (paymentTermsDays == null) {
            paymentTermsDays = 21;
        }

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }

        if (creditLimit == null) {
            creditLimit = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCblOutletId() {
        return cblOutletId;
    }

    public void setCblOutletId(String cblOutletId) {
        this.cblOutletId = cblOutletId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Integer getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public void setPaymentTermsDays(Integer paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }

    public String getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(String assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}