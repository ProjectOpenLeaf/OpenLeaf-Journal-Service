package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal {
    private Long id;
    private String keycloakUserId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
