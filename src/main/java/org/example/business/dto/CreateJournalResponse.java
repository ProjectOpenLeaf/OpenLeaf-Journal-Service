package org.example.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJournalResponse {
    private Long id;
    private String keycloakUserId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
