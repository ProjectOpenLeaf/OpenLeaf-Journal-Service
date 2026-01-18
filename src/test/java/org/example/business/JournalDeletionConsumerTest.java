package org.example.business;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.business.dto.AccountDeletionEvent;
import org.example.persistance.JournalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalDeletionConsumerTest {

    @Mock
    private JournalRepository journalRepository;

    @InjectMocks
    private JournalDeletionConsumer journalDeletionConsumer;

    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Setup logger to capture log statements
        logger = (Logger) LoggerFactory.getLogger(JournalDeletionConsumer.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @Test
    void handleAccountDeletion_WithMultipleJournals_DeletesSuccessfully() {
        // Arrange
        String userKeycloakId = "test-user-123";
        String reason = "User requested account deletion";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenReturn(5);

        // Act
        journalDeletionConsumer.handleAccountDeletion(event);

        // Assert
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        // Verify logging
        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(3, logEvents.size());

        // First log - received event
        assertEquals(Level.INFO, logEvents.get(0).getLevel());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("Received account deletion event for user: " + userKeycloakId));
        assertTrue(logEvents.get(0).getFormattedMessage().contains(reason));

        // Second log - deleted count
        assertEquals(Level.INFO, logEvents.get(1).getLevel());
        assertTrue(logEvents.get(1).getFormattedMessage().contains("Deleted 5 journal entries for user"));

        // Third log - success
        assertEquals(Level.INFO, logEvents.get(2).getLevel());
        assertTrue(logEvents.get(2).getFormattedMessage().contains("Successfully processed account deletion for user: " + userKeycloakId));
    }

    @Test
    void handleAccountDeletion_WithSingleJournal_DeletesSuccessfully() {
        // Arrange
        String userKeycloakId = "user-with-one-journal";
        String reason = "Privacy concerns";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenReturn(1);

        // Act
        journalDeletionConsumer.handleAccountDeletion(event);

        // Assert
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(3, logEvents.size());

        assertTrue(logEvents.get(1).getFormattedMessage().contains("Deleted 1 journal entries for user"));
    }

    @Test
    void handleAccountDeletion_WithNoJournals_ProcessesSuccessfully() {
        // Arrange
        String userKeycloakId = "user-without-journals";
        String reason = "Account migration";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenReturn(0);

        // Act
        journalDeletionConsumer.handleAccountDeletion(event);

        // Assert
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        List<ILoggingEvent> logEvents = logAppender.list;
        assertEquals(3, logEvents.size());

        assertTrue(logEvents.get(1).getFormattedMessage().contains("Deleted 0 journal entries for user"));
        assertTrue(logEvents.get(2).getFormattedMessage().contains("Successfully processed account deletion"));
    }

    @Test
    void handleAccountDeletion_WhenRepositoryThrowsException_LogsErrorAndRethrows() {
        // Arrange
        String userKeycloakId = "problematic-user";
        String reason = "System cleanup";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenThrow(repositoryException);

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            journalDeletionConsumer.handleAccountDeletion(event);
        });

        assertEquals("Database connection failed", thrown.getMessage());
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        // Verify error logging
        List<ILoggingEvent> logEvents = logAppender.list;
        assertTrue(logEvents.size() >= 2);

        // First log - received event
        assertEquals(Level.INFO, logEvents.get(0).getLevel());
        assertTrue(logEvents.get(0).getFormattedMessage().contains("Received account deletion event for user: " + userKeycloakId));

        // Error log
        ILoggingEvent errorLog = logEvents.stream()
                .filter(event1 -> event1.getLevel() == Level.ERROR)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected ERROR log not found"));

        assertTrue(errorLog.getFormattedMessage().contains("Failed to process account deletion event for user: " + userKeycloakId));
        assertNotNull(errorLog.getThrowableProxy());
    }

    @Test
    void handleAccountDeletion_WithDifferentReasons_LogsCorrectly() {
        // Arrange
        String userKeycloakId = "test-user-456";
        String reason = "GDPR data deletion request";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenReturn(3);

        // Act
        journalDeletionConsumer.handleAccountDeletion(event);

        // Assert
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        List<ILoggingEvent> logEvents = logAppender.list;
        assertTrue(logEvents.get(0).getFormattedMessage().contains("GDPR data deletion request"));
    }

    @Test
    void handleAccountDeletion_WhenExceptionOccurs_DoesNotSwallowException() {
        // Arrange
        String userKeycloakId = "user-123";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                "Test deletion"
        );

        IllegalStateException expectedException = new IllegalStateException("Transaction rollback required");
        when(journalRepository.deleteByKeycloakUserId(anyString())).thenThrow(expectedException);

        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            journalDeletionConsumer.handleAccountDeletion(event);
        });

        assertSame(expectedException, thrown);
        assertEquals("Transaction rollback required", thrown.getMessage());
    }

    @Test
    void handleAccountDeletion_VerifiesEventFieldsAreUsed() {
        // Arrange
        String userKeycloakId = "verification-user";
        String reason = "Verification test";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenReturn(2);

        // Act
        journalDeletionConsumer.handleAccountDeletion(event);

        // Assert
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        // Verify that both event fields are logged
        List<ILoggingEvent> logEvents = logAppender.list;
        String firstLogMessage = logEvents.get(0).getFormattedMessage();
        assertTrue(firstLogMessage.contains(userKeycloakId));
        assertTrue(firstLogMessage.contains(reason));
    }

    @Test
    void handleAccountDeletion_WithLargeNumberOfJournals_ProcessesCorrectly() {
        // Arrange
        String userKeycloakId = "power-user";
        String reason = "Account closure";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                reason
        );

        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenReturn(1000);

        // Act
        journalDeletionConsumer.handleAccountDeletion(event);

        // Assert
        verify(journalRepository, times(1)).deleteByKeycloakUserId(userKeycloakId);

        List<ILoggingEvent> logEvents = logAppender.list;
        assertTrue(logEvents.get(1).getFormattedMessage().contains("Deleted 1000 journal entries for user"));
    }

    @Test
    void handleAccountDeletion_ExceptionInCatchBlock_PropagatesCorrectly() {
        // Arrange
        String userKeycloakId = "exception-user";
        AccountDeletionEvent event = new AccountDeletionEvent(
                userKeycloakId,
                LocalDateTime.now(),
                "Exception test"
        );

        NullPointerException expectedException = new NullPointerException("Critical error");
        when(journalRepository.deleteByKeycloakUserId(userKeycloakId)).thenThrow(expectedException);

        // Act & Assert
        NullPointerException thrown = assertThrows(NullPointerException.class, () -> {
            journalDeletionConsumer.handleAccountDeletion(event);
        });

        assertEquals("Critical error", thrown.getMessage());

        // Verify exception is logged before being rethrown
        List<ILoggingEvent> logEvents = logAppender.list;
        boolean hasErrorLog = logEvents.stream()
                .anyMatch(log -> log.getLevel() == Level.ERROR &&
                        log.getFormattedMessage().contains("Failed to process account deletion event"));
        assertTrue(hasErrorLog, "Expected error log not found");
    }
}