package com.example.backend.services;

import com.example.backend.entities.dto.BlockingRequestDTO;
import com.example.backend.entities.dto.BlockingResponseDTO;

public interface BlockingService {
    BlockingResponseDTO processBlockingRequest(BlockingRequestDTO requestDto, String username);
    BlockingResponseDTO processBatchBlockingRequest(BlockingRequestDTO requestDto, String username);
}
