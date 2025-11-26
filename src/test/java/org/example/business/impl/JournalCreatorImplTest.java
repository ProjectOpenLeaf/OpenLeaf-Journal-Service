package org.example.business.impl;

import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JournalCreatorImplTest {

    private JournalRepository journalRepository;
    private JournalCreatorImpl journalCreator;

    @BeforeEach
    void setUp() {
        journalRepository = mock(JournalRepository.class);
        journalCreator = new JournalCreatorImpl(journalRepository);
    }

    @Test
    void create_ShouldSaveJournalEntity_AndReturnJournal() {

        // Arrange
        String keycloakUserId = "user123";
        String content = "My new journal entry";

        // Capture the entity passed to the repository
        ArgumentCaptor<JournalEntity> captor = ArgumentCaptor.forClass(JournalEntity.class);

        // Simulate repository returning entity with ID
        JournalEntity savedEntity = JournalEntity.builder()
                .id(1L)
                .keycloakUserId(keycloakUserId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(journalRepository.save(any(JournalEntity.class))).thenReturn(savedEntity);

        // Act
        Journal result = journalCreator.create(keycloakUserId, content);

        // Assert repository interaction
        verify(journalRepository).save(captor.capture());
        JournalEntity passedEntity = captor.getValue();

        assertThat(passedEntity.getKeycloakUserId()).isEqualTo(keycloakUserId);
        assertThat(passedEntity.getContent()).isEqualTo(content);
        assertThat(passedEntity.getCreatedAt()).isNotNull();
        assertThat(passedEntity.getUpdatedAt()).isNotNull();

        // Assert returned domain object
        assertThat(result.getId()).isEqualTo(savedEntity.getId());
        assertThat(result.getKeycloakUserId()).isEqualTo(keycloakUserId);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getCreatedAt()).isEqualTo(savedEntity.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(savedEntity.getUpdatedAt());
    }
}
