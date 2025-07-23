package com.example.backend.services;

import com.example.backend.entities.dto.FraudAlertDetailDTO;
import com.example.backend.entities.dto.FraudAlertResponseDTO;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class FraudAlertExportService {
    @Autowired
    private FraudAlertService fraudAlertService;

    @Async
    public CompletableFuture<StreamingResponseBody> exportFraudAlerts(
            String fileType, String startDate, String endDate, HttpServletResponse response) {

        if ("csv".equalsIgnoreCase(fileType)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=fraud_alerts.csv");
            return CompletableFuture.completedFuture(exportCsv(startDate, endDate));
        } else if ("xlsx".equalsIgnoreCase(fileType)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=fraud_alerts.xlsx");
            return CompletableFuture.completedFuture(exportXlsx(startDate, endDate));
        } else {
            throw new IllegalArgumentException("Invalid file type: " + fileType);
        }
    }

    private StreamingResponseBody exportCsv(String startDate, String endDate) {
        return outputStream -> {
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
                // Write headers
                writer.writeNext(new String[]{"MSISDN", "Unique Rules", "Total Alerts", "First Detection", "Last Detection",
                        "Rule Name", "Rule ID", "Start Date", "End Date", "Detection Date", "Occurrences"});

                List<FraudAlertResponseDTO> fraudAlerts = fraudAlertService.getFraudAlerts(startDate, endDate);
                int totalRecords = fraudAlerts.size();
                int processedRecords = 0;

                for (FraudAlertResponseDTO alert : fraudAlerts) {
                    List<FraudAlertDetailDTO> details = fraudAlertService.getFraudAlertsDetails(alert.getMsisdn(), LocalDate.parse(startDate), LocalDate.parse(startDate));
                    alert.setDetails(details);

                    for (FraudAlertDetailDTO detail : details) {
                        writer.writeNext(new String[]{
                                alert.getMsisdn(),
                                alert.getUniqueRules().toString(),
                                alert.getTotalAlerts().toString(),
                                alert.getFirstDateDetection().toString(),
                                alert.getLastDateDetection().toString(),
                                detail.getRuleName(),
                                detail.getRuleId().toString(),
                                detail.getStartDate(),
                                detail.getEndDate(),
                                detail.getDetectionDate().toString(),
                                detail.getOccurrences().toString()
                        });
                    }

                    processedRecords++;
                    outputStream.flush(); // Ensure real-time download progress
                }
            }
        };
    }

    private StreamingResponseBody exportXlsx(String startDate, String endDate) {
        return outputStream -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Fraud_Alerts");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"MSISDN", "Unique Rules", "Total Alerts", "First Detection", "Last Detection",
                        "Rule Name", "Rule ID", "Start Date", "End Date", "Detection Date", "Occurrences"};

                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                List<FraudAlertResponseDTO> fraudAlerts = fraudAlertService.getFraudAlerts(startDate, endDate);
                int rowNum = 1;
                int processedRecords = 0;
                int totalRecords = fraudAlerts.size();

                for (FraudAlertResponseDTO alert : fraudAlerts) {
                    List<FraudAlertDetailDTO> details = fraudAlertService.getFraudAlertsDetails(alert.getMsisdn(), LocalDate.parse(startDate), LocalDate.parse(endDate));
                    alert.setDetails(details);

                    for (FraudAlertDetailDTO detail : details) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(alert.getMsisdn());
                        row.createCell(1).setCellValue(alert.getUniqueRules());
                        row.createCell(2).setCellValue(alert.getTotalAlerts());
                        row.createCell(3).setCellValue(alert.getFirstDateDetection().toString());
                        row.createCell(4).setCellValue(alert.getLastDateDetection().toString());
                        row.createCell(5).setCellValue(detail.getRuleName());
                        row.createCell(6).setCellValue(detail.getRuleId());
                        row.createCell(7).setCellValue(detail.getStartDate());
                        row.createCell(8).setCellValue(detail.getEndDate());
                        row.createCell(9).setCellValue(detail.getDetectionDate().toString());
                        row.createCell(10).setCellValue(detail.getOccurrences());
                    }

                    processedRecords++;
                    outputStream.flush(); // Stream partial file data for download progress
                }

                workbook.write(outputStream);
            }
        };
    }
}
