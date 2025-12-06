package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.GetAllJournals;
import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllJournalsImpl implements GetAllJournals {

    private final JournalRepository journalRepository;

    @Override
    public List<Journal> getAllByUser(String keycloakUserId) {
        List<JournalEntity> entities = journalRepository.findByKeycloakUserId(keycloakUserId);
        return entities.stream()
                .map(this::toJournal)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Journal> getAllByUserPaginated(String keycloakUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JournalEntity> entityPage = journalRepository.findByKeycloakUserIdOrderByCreatedAtDesc(
                keycloakUserId,
                pageable
        );

        return entityPage.map(this::toJournal);
    }

    private Journal toJournal(JournalEntity entity) {
        return Journal.builder()
                .id(entity.getId())
                .keycloakUserId(entity.getKeycloakUserId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}