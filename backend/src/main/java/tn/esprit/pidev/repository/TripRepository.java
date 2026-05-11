package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Trip;
import java.util.List;

@Repository
public interface TripRepository
        extends JpaRepository<Trip, Long> {

    List<Trip> findByScheduleId(Long scheduleId);
    List<Trip> findByCompleted(Boolean completed);
}