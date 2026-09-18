package lk.aak.agency.repository;

import lk.aak.agency.model.EmployeeAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeAdvanceRepository extends JpaRepository<EmployeeAdvance, Long> {

    List<EmployeeAdvance> findAllByOrderByAdvanceDateDesc();

    List<EmployeeAdvance> findByEmployeeIdOrderByAdvanceDateDesc(Long employeeId);

    List<EmployeeAdvance> findByEmployeeIdAndSettledFalseOrderByAdvanceDateAsc(Long employeeId);
}
