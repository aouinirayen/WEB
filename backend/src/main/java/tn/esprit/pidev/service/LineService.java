package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.Line;
import tn.esprit.pidev.entity.enums.LineStatus;
import tn.esprit.pidev.entity.enums.TransportMode;
import tn.esprit.pidev.repository.LineRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LineService {

    private final LineRepository repository;

    public List<Line> getAll() {
        return repository.findAll();
    }

    public Optional<Line> getById(Long id) {
        return repository.findById(id);
    }

    public Line create(Line line) {
        return repository.save(line);
    }

    public Line update(Long id, Line line) {
        line.setId(id);
        return repository.save(line);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Line> getByStatus(LineStatus status) {
        return repository.findByStatus(status);
    }

    public List<Line> getByMode(TransportMode mode) {
        return repository.findByMode(mode);
    }
}