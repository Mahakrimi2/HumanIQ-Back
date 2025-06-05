package pfe.HumanIQ.HumanIQ.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pfe.HumanIQ.HumanIQ.models.Payslip;

import java.util.Date;
import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
/*
    List<Payslip> findByMonth(Date month);
*/

    <T> List<T> findByUserUsername(String username, Class<T> type);

    List<Payslip> findByUserId(Long userId);
}
