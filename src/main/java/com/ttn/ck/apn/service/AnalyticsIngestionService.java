package com.ttn.ck.apn.service;

import com.ttn.ck.apn.model.SubmittedOpportunity;
import com.ttn.ck.apn.repository.SubmittedOpportunityRepository;
import com.ttn.ck.errorhandler.exceptions.GenericStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses Analysis Excel files (historical submitted opportunity data) and upserts
 * records into the submitted_opportunity table. FR-7.1
 *
 * Expected Excel layout (row 1 = header, then data rows):
 *   Col A: Customer Name
 *   Col B: Year
 *   Col C: Month (1-12)
 *   Col D: Opportunity Count
 *   Col E: MRR
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsIngestionService {

    private final SubmittedOpportunityRepository submittedOpportunityRepository;

    @Transactional
    public int processAnalysisUpload(MultipartFile file) {
        log.info("Processing Analysis Excel: {}", file.getOriginalFilename());
        List<SubmittedOpportunity> toSave = new ArrayList<>();
        int rowsProcessed = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) { isFirstRow = false; continue; } // skip header
                if (isRowEmpty(row)) continue;

                String customerName = getStringValue(row.getCell(0));
                Integer year        = getIntValue(row.getCell(1));
                Integer month       = getIntValue(row.getCell(2));
                Integer count       = getIntValue(row.getCell(3));
                BigDecimal mrr      = getBigDecimalValue(row.getCell(4));

                if (customerName == null || customerName.isBlank()) continue;
                if (year == null || month == null) continue;

                // Find existing record for same customer+year+month and upsert
                final String finalCustomerName = customerName;
                final int finalYear = year;
                final int finalMonth = month;

                SubmittedOpportunity existing = submittedOpportunityRepository
                        .findByCustomerName(customerName)
                        .stream()
                        .filter(s -> s.getYear() != null && s.getYear() == finalYear
                                  && s.getMonth() != null && s.getMonth() == finalMonth)
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    existing.setOpportunityCount(count);
                    existing.setMrr(mrr);
                    toSave.add(existing);
                } else {
                    toSave.add(SubmittedOpportunity.builder()
                            .customerName(finalCustomerName)
                            .year(finalYear)
                            .month(finalMonth)
                            .opportunityCount(count)
                            .mrr(mrr)
                            .build());
                }
                rowsProcessed++;
            }

            submittedOpportunityRepository.saveAll(toSave);
            log.info("Analysis upload complete. Rows upserted: {}", rowsProcessed);
            return rowsProcessed;

        } catch (Exception e) {
            log.error("Failed to parse Analysis Excel: ", e);
            throw new GenericStatusException("Failed to process Analysis Excel: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> null;
        };
    }

    private Integer getIntValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING  -> {
                try { yield Integer.parseInt(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield null; }
            }
            default      -> null;
        };
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING  -> {
                try { yield new BigDecimal(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield BigDecimal.ZERO; }
            }
            default      -> BigDecimal.ZERO;
        };
    }
}
