package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for DocumentType entity
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

    /**
     * Find document type by name (unique)
     */
    Optional<DocumentType> findByName(String name);

    /**
     * Search document types by name or description
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "(LOWER(dt.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dt.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<DocumentType> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
