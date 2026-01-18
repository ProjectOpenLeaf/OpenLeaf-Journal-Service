package org.example.business.impl;

import org.example.domain.Journal;
import org.example.persistance.JournalRepository;
import org.example.persistance.entity.JournalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllJournalsImplTest {

    @Mock
    private JournalRepository journalRepository;

    @InjectMocks
    private GetAllJournalsImpl getAllJournals;

    private JournalEntity journalEntity1;
    private JournalEntity journalEntity2;
    private String testKeycloakUserId;

    @BeforeEach
    void setUp() {
        testKeycloakUserId = "test-user-123";

        journalEntity1 = JournalEntity.builder()
                .id(1L)
                .keycloakUserId(testKeycloakUserId)
                .content("First journal entry")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        journalEntity2 = JournalEntity.builder()
                .id(2L)
                .keycloakUserId(testKeycloakUserId)
                .content("Second journal entry")
                .createdAt(LocalDateTime.of(2024, 1, 2, 14, 30))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 15, 0))
                .build();
    }

    @Test
    void getAllByUser_WithMultipleEntries_ReturnsAllJournals() {
        // Arrange
        List<JournalEntity> entities = Arrays.asList(journalEntity1, journalEntity2);
        when(journalRepository.findByKeycloakUserId(testKeycloakUserId)).thenReturn(entities);

        // Act
        List<Journal> result = getAllJournals.getAllByUser(testKeycloakUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        Journal journal1 = result.get(0);
        assertEquals(1L, journal1.getId());
        assertEquals(testKeycloakUserId, journal1.getKeycloakUserId());
        assertEquals("First journal entry", journal1.getContent());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), journal1.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), journal1.getUpdatedAt());

        Journal journal2 = result.get(1);
        assertEquals(2L, journal2.getId());
        assertEquals(testKeycloakUserId, journal2.getKeycloakUserId());
        assertEquals("Second journal entry", journal2.getContent());
        assertEquals(LocalDateTime.of(2024, 1, 2, 14, 30), journal2.getCreatedAt());
        assertEquals(LocalDateTime.of(2024, 1, 2, 15, 0), journal2.getUpdatedAt());

        verify(journalRepository, times(1)).findByKeycloakUserId(testKeycloakUserId);
    }

    @Test
    void getAllByUser_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        when(journalRepository.findByKeycloakUserId(testKeycloakUserId)).thenReturn(Collections.emptyList());

        // Act
        List<Journal> result = getAllJournals.getAllByUser(testKeycloakUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(journalRepository, times(1)).findByKeycloakUserId(testKeycloakUserId);
    }

    @Test
    void getAllByUser_WithSingleEntry_ReturnsSingleJournal() {
        // Arrange
        List<JournalEntity> entities = Collections.singletonList(journalEntity1);
        when(journalRepository.findByKeycloakUserId(testKeycloakUserId)).thenReturn(entities);

        // Act
        List<Journal> result = getAllJournals.getAllByUser(testKeycloakUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(journalRepository, times(1)).findByKeycloakUserId(testKeycloakUserId);
    }

    @Test
    void getAllByUserPaginated_WithMultipleEntries_ReturnsPaginatedJournals() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<JournalEntity> entities = Arrays.asList(journalEntity1, journalEntity2);
        Page<JournalEntity> entityPage = new PageImpl<>(entities, pageable, entities.size());

        when(journalRepository.findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class)))
                .thenReturn(entityPage);

        // Act
        Page<Journal> result = getAllJournals.getAllByUserPaginated(testKeycloakUserId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(page, result.getNumber());
        assertEquals(size, result.getSize());

        Journal journal1 = result.getContent().get(0);
        assertEquals(1L, journal1.getId());
        assertEquals("First journal entry", journal1.getContent());

        Journal journal2 = result.getContent().get(1);
        assertEquals(2L, journal2.getId());
        assertEquals("Second journal entry", journal2.getContent());

        verify(journalRepository, times(1))
                .findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class));
    }

    @Test
    void getAllByUserPaginated_WithEmptyPage_ReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<JournalEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(journalRepository.findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<Journal> result = getAllJournals.getAllByUserPaginated(testKeycloakUserId, page, size);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());

        verify(journalRepository, times(1))
                .findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class));
    }

    @Test
    void getAllByUserPaginated_WithSecondPage_ReturnsPaginatedJournals() {
        // Arrange
        int page = 1;
        int size = 1;
        Pageable pageable = PageRequest.of(page, size);
        List<JournalEntity> entities = Collections.singletonList(journalEntity2);
        Page<JournalEntity> entityPage = new PageImpl<>(entities, pageable, 2);

        when(journalRepository.findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class)))
                .thenReturn(entityPage);

        // Act
        Page<Journal> result = getAllJournals.getAllByUserPaginated(testKeycloakUserId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
        assertEquals(1, result.getSize());

        Journal journal = result.getContent().get(0);
        assertEquals(2L, journal.getId());
        assertEquals("Second journal entry", journal.getContent());

        verify(journalRepository, times(1))
                .findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class));
    }

    @Test
    void getAllByUserPaginated_WithDifferentPageSizes_ReturnsPaginatedJournals() {
        // Arrange
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        List<JournalEntity> entities = Collections.singletonList(journalEntity1);
        Page<JournalEntity> entityPage = new PageImpl<>(entities, pageable, 1);

        when(journalRepository.findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class)))
                .thenReturn(entityPage);

        // Act
        Page<Journal> result = getAllJournals.getAllByUserPaginated(testKeycloakUserId, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(size, result.getSize());

        verify(journalRepository, times(1))
                .findByKeycloakUserIdOrderByCreatedAtDesc(eq(testKeycloakUserId), any(Pageable.class));
    }

    @Test
    void getAllByUser_VerifiesMappingAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 9, 30, 45);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 10, 15, 30);

        JournalEntity entity = JournalEntity.builder()
                .id(999L)
                .keycloakUserId("unique-user-id")
                .content("Detailed journal content")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(journalRepository.findByKeycloakUserId("unique-user-id"))
                .thenReturn(Collections.singletonList(entity));

        // Act
        List<Journal> result = getAllJournals.getAllByUser("unique-user-id");

        // Assert
        assertEquals(1, result.size());
        Journal journal = result.get(0);

        assertEquals(999L, journal.getId());
        assertEquals("unique-user-id", journal.getKeycloakUserId());
        assertEquals("Detailed journal content", journal.getContent());
        assertEquals(createdAt, journal.getCreatedAt());
        assertEquals(updatedAt, journal.getUpdatedAt());
    }

    @Test
    void getAllByUserPaginated_VerifiesMappingAllFields() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 20, 14, 45, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 20, 15, 30, 15);

        JournalEntity entity = JournalEntity.builder()
                .id(888L)
                .keycloakUserId("another-user-id")
                .content("Paginated journal content")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<JournalEntity> entityPage = new PageImpl<>(Collections.singletonList(entity), pageable, 1);

        when(journalRepository.findByKeycloakUserIdOrderByCreatedAtDesc(eq("another-user-id"), any(Pageable.class)))
                .thenReturn(entityPage);

        // Act
        Page<Journal> result = getAllJournals.getAllByUserPaginated("another-user-id", 0, 10);

        // Assert
        assertEquals(1, result.getContent().size());
        Journal journal = result.getContent().get(0);

        assertEquals(888L, journal.getId());
        assertEquals("another-user-id", journal.getKeycloakUserId());
        assertEquals("Paginated journal content", journal.getContent());
        assertEquals(createdAt, journal.getCreatedAt());
        assertEquals(updatedAt, journal.getUpdatedAt());
    }
}