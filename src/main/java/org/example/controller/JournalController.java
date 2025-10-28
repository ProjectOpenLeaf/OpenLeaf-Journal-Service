package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.business.GetAllJournals;
import org.example.business.GetJournalById;
import org.example.business.JournalCreator;
import org.example.business.dto.CreateJournalRequest;
import org.example.business.dto.CreateJournalResponse;
import org.example.business.dto.GetJournalResponse;
import org.example.domain.Journal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
//@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalCreator journalCreator;
    private final GetAllJournals getAllJournals;
    private final GetJournalById getJournalById;

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

    @GetMapping
    public ResponseEntity<List<GetJournalResponse>> getAllJournals(
            @RequestHeader("X-User-Id") String keycloakUserId) {

        List<Journal> journals = getAllJournals.getAllByUser(keycloakUserId);

        List<GetJournalResponse> responses = journals.stream()
                .map(journal -> GetJournalResponse.builder()
                        .id(journal.getId())
                        .keycloakUserId(journal.getKeycloakUserId())
                        .content(journal.getContent())
                        .createdAt(journal.getCreatedAt())
                        .updatedAt(journal.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetJournalResponse> getJournalById(
            @RequestHeader("X-User-Id") String keycloakUserId,
            @PathVariable Long id) {

        return getJournalById.getById(id, keycloakUserId)
                .map(journal -> GetJournalResponse.builder()
                        .id(journal.getId())
                        .keycloakUserId(journal.getKeycloakUserId())
                        .content(journal.getContent())
                        .createdAt(journal.getCreatedAt())
                        .updatedAt(journal.getUpdatedAt())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
