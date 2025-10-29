package org.example.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.business.dto.AccountDeletionEvent;
import org.example.config.RabbitMQConfig;
import org.example.persistance.JournalRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer for account deletion events in Journal Service
 * Deletes all journal entries related to the deleted user
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JournalDeletionConsumer {

    private final JournalRepository journalRepository;
    // If you have Azure Blob Storage service, inject it here to delete images/videos

    /**
     * Listen for account deletion events and delete related journal entries
     *
     * @param event The account deletion event
     */
    @RabbitListener(queues = RabbitMQConfig.JOURNAL_DELETION_QUEUE)
    @Transactional
    public void handleAccountDeletion(AccountDeletionEvent event) {
        try {
            String userKeycloakId = event.getUserKeycloakId();
            log.info("Received account deletion event for user: {} - Reason: {}",
                    userKeycloakId, event.getReason());

            // TODO: If using Azure Blob Storage, delete all images/videos first
            // Example:
            // List<JournalEntity> journals = journalRepository.findByUserKeycloakId(userKeycloakId);
            // for (JournalEntity journal : journals) {
            //     if (journal.getImageUrl() != null) {
            //         azureBlobService.deleteBlob(journal.getImageUrl());
            //     }
            // }

            // Delete all journal entries for the user
            int deletedJournals = journalRepository.deleteByKeycloakUserId(userKeycloakId);
            log.info("Deleted {} journal entries for user", deletedJournals);

            log.info("Successfully processed account deletion for user: {} in Journal Service",
                    userKeycloakId);

        } catch (Exception e) {
            log.error("Failed to process account deletion event for user: {}",
                    event.getUserKeycloakId(), e);
            // In production, you might want to implement a dead letter queue
            // or retry mechanism here
            throw e; // This will trigger RabbitMQ redelivery
        }
    }
}







