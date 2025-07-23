package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockingResponseDTO {
    private String message;
    private boolean success;
    private List<NumberBlockingResult> results;
}
