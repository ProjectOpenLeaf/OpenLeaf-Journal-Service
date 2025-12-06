package org.example.business;

import org.example.domain.Journal;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GetAllJournals {
    List<Journal> getAllByUser(String keycloakUserId);

    // Paginated version
    Page<Journal> getAllByUserPaginated(String keycloakUserId, int page, int size);
}
