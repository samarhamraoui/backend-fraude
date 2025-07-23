package com.example.backend.services;

import com.example.backend.dao.FraudAlertRepository;
import com.example.backend.entities.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class FraudAlertService {

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    public List<FraudAlertResponseDTO> getFraudAlerts(String startDate, String endDate) {
        return fraudAlertRepository.getFraudAlerts(startDate, endDate);
    }

    public List<FraudAlertDetailDTO> getFraudAlertsDetails(String startDate, LocalDate endDate, LocalDate msisdn) {
        return fraudAlertRepository.getFraudAlertDetails(startDate, endDate,msisdn);
    }

    public List<WarningResponseDTO> getWarnings(String startDate, String endDate) {
        List<WarningResponseDTO> warnings = fraudAlertRepository.getWarnings(startDate, endDate);
        return warnings;
    }

    public List<WarningDetailDTO> getWarningsDetails(Long ruleId,String startDate, String endDate) {
        List<WarningDetailDTO> warnings = fraudAlertRepository.getWarningDetails(ruleId,startDate, endDate);
        return warnings;
    }


    public Page<WarningResponseDTO> getWarningsV2(String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("last_date_detection").descending());

        List<WarningResponseDTO> warningsList = fraudAlertRepository.getWarningsV2(startDate, endDate, pageable);

        int start = Math.min((int) pageable.getOffset(), warningsList.size());
        int end = Math.min(start + pageable.getPageSize(), warningsList.size());

        return new PageImpl<>(warningsList.subList(start, end), pageable, warningsList.size());
    }

    public List<WarningParamsDto> getWarningParams(String msisdn, Integer idRule){
        return fraudAlertRepository.getWarningParams(msisdn,idRule);
    }


    // ✅ Export to CSV
    public byte[] exportToCsv(String startDate, String endDate) {
        List<FraudAlertResponseDTO> fraudAlerts = getFraudAlerts(startDate, endDate);
        StringBuilder csvData = new StringBuilder();

        // Header
        csvData.append("MSISDN,Unique Rules,Total Alerts,First Detection,Last Detection,Rule Name,Rule ID,Start Date,End Date,Detection Date,Occurrences,Extra Date\n");

        // Data Rows
        for (FraudAlertResponseDTO alert : fraudAlerts) {
            boolean firstRow = true;
            for (FraudAlertDetailDTO detail : alert.getDetails()) {
                csvData.append(alert.getMsisdn()).append(",")
                        .append(alert.getUniqueRules()).append(",")
                        .append(alert.getTotalAlerts()).append(",")
                        .append(alert.getFirstDateDetection()).append(",")
                        .append(alert.getLastDateDetection()).append(",");

                csvData.append(detail.getRuleName()).append(",")
                        .append(detail.getRuleId()).append(",")
                        .append(detail.getStartDate()).append(",")
                        .append(detail.getEndDate()).append(",")
                        .append(detail.getDetectionDate()).append(",")
                        .append(detail.getOccurrences()).append(",")
                        .append(detail.getDatee()).append("\n");

                firstRow = false;
            }
        }
        return csvData.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ✅ Export to XLSX
    public byte[] exportToXlsx(String startDate, String endDate) throws IOException {
        List<FraudAlertResponseDTO> fraudAlerts = getFraudAlerts(startDate, endDate);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fraud Alerts");

        // 🎨 Header Row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"MSISDN", "Unique Rules", "Total Alerts", "First Detection", "Last Detection",
                "Rule Name", "Rule ID", "Start Date", "End Date", "Detection Date", "Occurrences", "Extra Date"};

        // Style header
        CellStyle headerStyle = getHeaderCellStyle(workbook);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // 📌 Fill Data
        int rowIdx = 1;
        for (FraudAlertResponseDTO alert : fraudAlerts) {
            boolean firstRow = true;
            for (FraudAlertDetailDTO detail : alert.getDetails()) {
                Row row = sheet.createRow(rowIdx++);

                if (firstRow) {
                    row.createCell(0).setCellValue(alert.getMsisdn());
                    row.createCell(1).setCellValue(alert.getUniqueRules());
                    row.createCell(2).setCellValue(alert.getTotalAlerts());
                    row.createCell(3).setCellValue(alert.getFirstDateDetection().toString());
                    row.createCell(4).setCellValue(alert.getLastDateDetection().toString());
                    firstRow = false;
                }

                row.createCell(5).setCellValue(detail.getRuleName());
                row.createCell(6).setCellValue(detail.getRuleId());
                row.createCell(7).setCellValue(detail.getStartDate());
                row.createCell(8).setCellValue(detail.getEndDate());
                row.createCell(9).setCellValue(detail.getDetectionDate().toString());
                row.createCell(10).setCellValue(detail.getOccurrences());
                row.createCell(11).setCellValue(detail.getDatee());
            }
        }

        // Convert to Byte Array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // 🎨 Header Styling
    private CellStyle getHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        return headerStyle;
    }
}
