package com.example.backend.Controllers;

import com.example.backend.entities.KpiMetadata;
import com.example.backend.entities.dto.KpiDTO;
import com.example.backend.entities.dto.KpiRequest;
import com.example.backend.services.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kpi")
public class KpiController {

    @Autowired
    private KpiService kpiService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<KpiMetadata>> getUserKpis(@PathVariable Long userId) {
        return ResponseEntity.ok(kpiService.getUserKpis(userId));
    }

    @PostMapping("/{kpiId}")
    public ResponseEntity<KpiDTO> getUserKpiData(@PathVariable Long kpiId,@RequestBody KpiRequest request) {
        KpiDTO kpiDto = kpiService.getKpiDTO(kpiId, request.getUserId(), request.getParams());
        return ResponseEntity.ok(kpiDto);
    }
}
