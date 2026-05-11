package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.Schedule;
import tn.esprit.pidev.entity.enums.DayType;
import tn.esprit.pidev.repository.ScheduleRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository repository;

    public List<Schedule> getAll() {
        return repository.findAll();
    }

    public Optional<Schedule> getById(Long id) {
        return repository.findById(id);
    }

    public Schedule create(Schedule schedule) {
        return repository.save(schedule);
    }

    public Schedule update(Long id, Schedule schedule) {
        schedule.setId(id);
        return repository.save(schedule);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Schedule> getByLine(Long lineId) {
        return repository.findByLineId(lineId);
    }

    public List<Schedule> getByDayType(DayType dayType) {
        return repository.findByDayType(dayType);
    }

    public List<Schedule> getByVehicle(Long vehicleId) {
        return repository.findByVehicleId(vehicleId);
    }
}
