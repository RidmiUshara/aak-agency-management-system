package lk.aak.agency.service;

import lk.aak.agency.model.Employee;
import lk.aak.agency.model.EmployeeAttendance;
import lk.aak.agency.repository.EmployeeAttendanceRepository;
import lk.aak.agency.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeAttendanceService {

    private final EmployeeAttendanceRepository employeeAttendanceRepository;
    private final EmployeeRepository employeeRepository;

    public EmployeeAttendanceService(
            EmployeeAttendanceRepository employeeAttendanceRepository,
            EmployeeRepository employeeRepository) {

        this.employeeAttendanceRepository = employeeAttendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Every active employee for the given date, paired with their attendance record if one
     * has already been marked (null otherwise) - the basis for the daily marking sheet.
     */
    public List<AttendanceRow> getAttendanceSheetForDate(LocalDate date) {

        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(employee -> "ACTIVE".equalsIgnoreCase(employee.getStatus()))
                .toList();

        Map<Long, EmployeeAttendance> attendanceByEmployeeId =
                employeeAttendanceRepository.findByAttendanceDate(date).stream()
                        .collect(Collectors.toMap(EmployeeAttendance::getEmployeeId, a -> a));

        List<AttendanceRow> rows = new ArrayList<>();

        for (Employee employee : activeEmployees) {
            rows.add(new AttendanceRow(employee, attendanceByEmployeeId.get(employee.getId())));
        }

        return rows;
    }

    public List<EmployeeAttendance> getAttendanceHistoryForEmployee(Long employeeId) {
        return employeeAttendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(employeeId);
    }

    @Transactional
    public void markAttendance(Long employeeId, LocalDate date, String status, String notes) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Selected employee was not found."));

        EmployeeAttendance attendance = employeeAttendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseGet(EmployeeAttendance::new);

        attendance.setEmployeeId(employeeId);
        attendance.setEmployeeName(employee.getFullName());
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setNotes(notes);

        employeeAttendanceRepository.save(attendance);
    }

    public record AttendanceRow(Employee employee, EmployeeAttendance attendance) {

        public String getStatus() {
            return attendance != null ? attendance.getStatus() : "NOT_MARKED";
        }
    }
}
