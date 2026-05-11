package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.Stop;
import tn.esprit.pidev.repository.StopRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StopService {

    private final StopRepository repository;

    public List<Stop> getAll() {
        return repository.findAll();
    }

    public Optional<Stop> getById(Long id) {
        return repository.findById(id);
    }

    public Stop create(Stop stop) {
        return repository.save(stop);
    }

    public Stop update(Long id, Stop stop) {
        stop.setId(id);
        return repository.save(stop);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Stop> getByLine(Long lineId) {
        return repository.findByLineIdOrderBySequenceAsc(lineId);
    }
}