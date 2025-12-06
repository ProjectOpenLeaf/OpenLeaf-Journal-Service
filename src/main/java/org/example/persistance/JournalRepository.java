package org.example.persistance;

import org.example.persistance.entity.JournalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalRepository extends JpaRepository<JournalEntity, Long> {
    List<JournalEntity> findByKeycloakUserId(String keycloakUserId);
    int deleteByKeycloakUserId(String keycloakUserId);
    Page<JournalEntity> findByKeycloakUserIdOrderByCreatedAtDesc(
            String keycloakUserId,
            Pageable pageable
    );
}
