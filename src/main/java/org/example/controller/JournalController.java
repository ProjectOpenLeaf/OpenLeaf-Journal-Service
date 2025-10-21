package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.business.JournalCreator;
import org.example.business.dto.CreateJournalRequest;
import org.example.business.dto.CreateJournalResponse;
import org.example.domain.Journal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalCreator journalCreator;

    @PostMapping("/create")
    public ResponseEntity<CreateJournalResponse> createJournal(
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestBody CreateJournalRequest request) {

        Journal journal = journalCreator.create(
                keycloakUserId,
                request.getContent()
        );

        CreateJournalResponse response = CreateJournalResponse.builder()
                .id(journal.getId())
                .keycloakUserId(journal.getKeycloakUserId())
                .content(journal.getContent())
                .createdAt(journal.getCreatedAt())
                .updatedAt(journal.getUpdatedAt())
                .message("Journal created successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}
