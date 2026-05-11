package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.AITrainingData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AITrainingDataRepository extends JpaRepository<AITrainingData, Long> {
    List<AITrainingData> findByDataType(String dataType);
    long countByDataType(String dataType);
}
