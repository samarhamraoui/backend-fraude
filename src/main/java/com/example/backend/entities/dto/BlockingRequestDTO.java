package com.example.backend.entities.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockingRequestDTO {
    private String phoneNumber;
    private BlockingType blockingType;  // "blocage", "deblocage", "WhiteList", "blocageSMS", "deblocageSMS"
    private NetworkType networkType;   // "onnet", "offnet"
    private List<String> phoneNumbers;  // For batch operations
}