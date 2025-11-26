package org.example.business.impl;

import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAllJournalsImplTest {

    private JournalRepository journalRepository;
    private GetAllJournalsImpl getAllJournals;

    @BeforeEach
    void setUp() {
        journalRepository = mock(JournalRepository.class);
        getAllJournals = new GetAllJournalsImpl(journalRepository);
    }

    @Test
    void getAllByUser_ShouldReturnMappedJournals_WhenEntitiesExist() {
        // Arrange
        String userId = "user123";

        JournalEntity e1 = JournalEntity.builder()
                .id(1L)
                .keycloakUserId(userId)
                .content("First journal")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        JournalEntity e2 = JournalEntity.builder()
                .id(2L)
                .keycloakUserId(userId)
                .content("Second journal")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(journalRepository.findByKeycloakUserId(userId))
                .thenReturn(List.of(e1, e2));

        // Act
        List<Journal> result = getAllJournals.getAllByUser(userId);

        // Assert
        assertEquals(2, result.size());

        Journal j1 = result.get(0);
        Journal j2 = result.get(1);

        assertEquals(e1.getId(), j1.getId());
        assertEquals(e1.getContent(), j1.getContent());
        assertEquals(e1.getCreatedAt(), j1.getCreatedAt());

        assertEquals(e2.getId(), j2.getId());
        assertEquals(e2.getContent(), j2.getContent());
        assertEquals(e2.getUpdatedAt(), j2.getUpdatedAt());
    }

    @Test
    void getAllByUser_ShouldReturnEmptyList_WhenNoJournalsExist() {
        // Arrange
        String userId = "user123";

        when(journalRepository.findByKeycloakUserId(userId))
                .thenReturn(List.of());

        // Act
        List<Journal> result = getAllJournals.getAllByUser(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByUser_ShouldCallRepositoryWithCorrectArgument() {
        // Arrange
        String userId = "userXYZ";

        when(journalRepository.findByKeycloakUserId(userId))
                .thenReturn(List.of());

        // Act
        getAllJournals.getAllByUser(userId);

        // Assert
        verify(journalRepository, times(1))
                .findByKeycloakUserId(userId);
    }
}
