package org.example.business;

import org.example.domain.Journal;

import java.util.List;

public interface GetAllJournals {
    List<Journal> getAllByUser(String keycloakUserId);
}
