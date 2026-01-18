package org.example.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.business.dto.AccountDeletionEvent;
import org.example.config.RabbitMQConfig;
import org.example.persistance.JournalRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class JournalDeletionConsumer {

    private final JournalRepository journalRepository;

    @RabbitListener(queues = RabbitMQConfig.JOURNAL_DELETION_QUEUE)
    @Transactional
    public void handleAccountDeletion(AccountDeletionEvent event) {
        try {
            String userKeycloakId = event.getUserKeycloakId();
            log.info("Received account deletion event for user: {} - Reason: {}",
                    userKeycloakId, event.getReason());

            int deletedJournals = journalRepository.deleteByKeycloakUserId(userKeycloakId);
            log.info("Deleted {} journal entries for user", deletedJournals);

            log.info("Successfully processed account deletion for user: {} in Journal Service",
                    userKeycloakId);

        } catch (Exception e) {
            log.error("Failed to process account deletion event for user: {}",
                    event.getUserKeycloakId(), e);

            throw e;
        }
    }
}







