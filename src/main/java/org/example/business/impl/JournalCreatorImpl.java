package org.example.business.impl;

import lombok.RequiredArgsConstructor;
import org.example.business.JournalCreator;
import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JournalCreatorImpl implements JournalCreator {

    private final JournalRepository journalRepository;

    @Override
    public Journal create(String keycloakUserId, String content) {
        JournalEntity journalEntity = JournalEntity.builder()
                .keycloakUserId(keycloakUserId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        JournalEntity savedEntity = journalRepository.save(journalEntity);

        return toJournal(savedEntity);
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