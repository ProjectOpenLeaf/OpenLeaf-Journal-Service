package org.example.business;

import org.example.domain.Journal;

public interface JournalCreator {
    Journal create(String keycloakUserId, String content);
}
