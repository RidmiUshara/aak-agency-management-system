package lk.aak.agency.repository;

import lk.aak.agency.model.EmployeeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Long> {

    Optional<EmployeeAttendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    List<EmployeeAttendance> findByAttendanceDate(LocalDate attendanceDate);

    List<EmployeeAttendance> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);
}
