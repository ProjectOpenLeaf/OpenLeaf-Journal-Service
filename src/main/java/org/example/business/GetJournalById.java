package org.example.business;

import org.example.domain.Journal;

import java.util.Optional;

public interface GetJournalById {
    Optional<Journal> getById(Long id, String keycloakUserId);
}
