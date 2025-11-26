package org.example.business.impl;

import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetJournalByIdImplTest {

    private JournalRepository journalRepository;
    private GetJournalByIdImpl getJournalById;

    @BeforeEach
    void setUp() {
        journalRepository = mock(JournalRepository.class);
        getJournalById = new GetJournalByIdImpl(journalRepository);
    }

    @Test
    void getById_ShouldReturnJournal_WhenIdMatchesAndUserMatches() {
        // Arrange
        Long journalId = 1L;
        String userId = "user123";

        JournalEntity entity = JournalEntity.builder()
                .id(journalId)
                .keycloakUserId(userId)
                .content("Test content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(journalRepository.findById(journalId)).thenReturn(Optional.of(entity));

        // Act
        Optional<Journal> result = getJournalById.getById(journalId, userId);

        // Assert
        assertTrue(result.isPresent());
        Journal journal = result.get();

        assertEquals(entity.getId(), journal.getId());
        assertEquals(entity.getKeycloakUserId(), journal.getKeycloakUserId());
        assertEquals(entity.getContent(), journal.getContent());
        assertEquals(entity.getCreatedAt(), journal.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), journal.getUpdatedAt());
    }

    @Test
    void getById_ShouldReturnEmpty_WhenJournalExistsButUserDoesNotMatch() {
        // Arrange
        Long journalId = 1L;
        String correctUserId = "user123";
        String wrongUserId = "hackerUser";

        JournalEntity entity = JournalEntity.builder()
                .id(journalId)
                .keycloakUserId(correctUserId)
                .content("Test content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(journalRepository.findById(journalId)).thenReturn(Optional.of(entity));

        // Act
        Optional<Journal> result = getJournalById.getById(journalId, wrongUserId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getById_ShouldReturnEmpty_WhenJournalDoesNotExist() {
        // Arrange
        Long journalId = 1L;
        String userId = "user123";

        when(journalRepository.findById(journalId)).thenReturn(Optional.empty());

        // Act
        Optional<Journal> result = getJournalById.getById(journalId, userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getById_ShouldCallRepositoryWithCorrectId() {
        // Arrange
        Long journalId = 5L;
        String userId = "userXYZ";

        when(journalRepository.findById(journalId))
                .thenReturn(Optional.empty());

        // Act
        getJournalById.getById(journalId, userId);

        // Assert
        verify(journalRepository, times(1)).findById(journalId);
    }
}
