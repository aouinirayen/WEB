package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Schedule;
import tn.esprit.pidev.entity.enums.DayType;
import java.util.List;

@Repository
public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    List<Schedule> findByLineId(Long lineId);
    List<Schedule> findByDayType(DayType dayType);
    List<Schedule> findByVehicleId(Long vehicleId);
}