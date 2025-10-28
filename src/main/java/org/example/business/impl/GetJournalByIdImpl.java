package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.GetJournalById;
import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetJournalByIdImpl implements GetJournalById {

    private final JournalRepository journalRepository;

    @Override
    public Optional<Journal> getById(Long id, String keycloakUserId) {
        return journalRepository.findById(id)
                .filter(journal -> journal.getKeycloakUserId().equals(keycloakUserId))
                .map(this::toJournal);
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
