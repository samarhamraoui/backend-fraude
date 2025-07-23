package com.example.backend.Controllers;

import com.example.backend.conf.JwtTokenUtil;
import com.example.backend.entities.Blocage;
import com.example.backend.entities.dto.*;
import com.example.backend.services.BlocageRepository;
import com.example.backend.services.BlockingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/api/blocking")
@Tag(name = "Controller for blocage", description = "API for blocage.")
public class BlocageController {
    @Autowired
    private BlocageRepository blocageRepository;

    @Autowired
    private BlockingService blockingService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/add")
    public String addBlocages(@RequestBody BlocageRequest blocageRequest) {
        String msisdnData = blocageRequest.getMsisdn();
        String type = blocageRequest.getType();

        List<String> msisdns = Arrays.asList(msisdnData.split("[,\\s\\n]+"));

        for (String msisdn : msisdns) {
            if (msisdn.length() == 8) {
                Blocage blocage = new Blocage();
                blocage.setMsisdn(msisdn);
                blocage.setType(type);
                blocageRepository.save(blocage);
            }
        }

        return "Blocage(s) added successfully";
    }

    @GetMapping
    public List<Blocage> getAllBlocages() {
        return blocageRepository.findAll();
    }

    @PostMapping("/execute")
    public ResponseEntity<BlockingResponseDTO> executeBlocking(@Valid @RequestBody BlockingRequestDTO requestDto,
                                                               @RequestHeader("Authorization") String authorizationHeader) {
        String token = jwtTokenUtil.extractToken(authorizationHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        BlockingResponseDTO response = blockingService.processBlockingRequest(requestDto, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<BlockingResponseDTO> executeBatchBlocking(@Valid @RequestBody BlockingRequestDTO requestDto,
                                                                    @RequestHeader("Authorization") String authorizationHeader) {
        String token = jwtTokenUtil.extractToken(authorizationHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        BlockingResponseDTO response = blockingService.processBatchBlockingRequest(requestDto, username);
        return ResponseEntity.ok(response);
    }

    // Additional endpoints for enums
    @GetMapping("/blocking-types")
    public ResponseEntity<BlockingType[]> getBlockingTypes() {
        return ResponseEntity.ok(BlockingType.values());
    }

    @GetMapping("/network-types")
    public ResponseEntity<NetworkType[]> getNetworkTypes() {
        return ResponseEntity.ok(NetworkType.values());
    }
}
