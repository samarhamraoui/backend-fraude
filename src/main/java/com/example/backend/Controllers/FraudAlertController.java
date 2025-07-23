package com.example.backend.Controllers;

import com.example.backend.entities.dto.*;
import com.example.backend.services.FraudAlertExportService;
import com.example.backend.services.FraudAlertService;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/fraud-alerts")
public class FraudAlertController {

    @Autowired
    private FraudAlertService fraudAlertService;
    @Autowired
    private FraudAlertExportService exportService;

    @GetMapping
    public List<FraudAlertResponseDTO> getFraudAlerts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) {
        return fraudAlertService.getFraudAlerts(startDate, endDate);
    }

    @GetMapping("/details")
    public List<FraudAlertDetailDTO> getFraudAlerts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String msisdn) {
        return fraudAlertService.getFraudAlertsDetails(msisdn, startDate, endDate);
    }

    @GetMapping("/alerts/export")
    public CompletableFuture<StreamingResponseBody> exportFraudAlerts(
            @RequestParam String fileType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate,
            HttpServletResponse response) {

        return exportService.exportFraudAlerts(fileType, startDate, endDate, response);
    }

    @GetMapping("/warnings")
    public List<WarningResponseDTO> getWarnings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) {
        return fraudAlertService.getWarnings(startDate, endDate);
    }

    @GetMapping("/v2/warnings")
    public ResponseEntity<?> getWarnings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate,
            @RequestParam(defaultValue = "0") int page,  // Default to first page
            @RequestParam(defaultValue = "10") int size // Default 10 results per page
    ) {
        Page<WarningResponseDTO> warnings = fraudAlertService.getWarningsV2(startDate, endDate, page, size);
        return ResponseEntity.ok(warnings.getContent());
    }

    @GetMapping("/warnings/details")
    public List<WarningDetailDTO> getWarningsDetails(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate,
            @RequestParam Long idRule) {
        return fraudAlertService.getWarningsDetails(idRule,startDate, endDate);
    }

    @GetMapping("/getWarningParams")
    public List<WarningParamsDto> getDetailsWarning(@RequestParam String msisdn,
                                                    @RequestParam Integer idRule) {
        return fraudAlertService.getWarningParams(msisdn,idRule);
    }

    @GetMapping(value = "/export")
    public ResponseEntity<byte[]> exportFraudAlerts(
            @RequestParam String fileType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) throws IOException {

        byte[] fileData;
        String filename;
        MediaType mediaType;

        if ("csv".equalsIgnoreCase(fileType)) {
            fileData = fraudAlertService.exportToCsv(startDate, endDate);
            filename = "Alerts_By_MSISDN.csv";
            mediaType = MediaType.TEXT_PLAIN;
        } else if ("xlsx".equalsIgnoreCase(fileType)) {
            fileData = fraudAlertService.exportToXlsx(startDate, endDate);
            filename = "Alerts_By_MSISDN.xlsx";
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            return ResponseEntity.badRequest().body("Invalid fileType. Use 'csv' or 'xlsx'.".getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileData.length))
                .contentType(mediaType)
                .body(fileData);
    }

}